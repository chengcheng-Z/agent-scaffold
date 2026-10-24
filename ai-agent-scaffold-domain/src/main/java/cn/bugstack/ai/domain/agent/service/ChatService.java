package cn.bugstack.ai.domain.agent.service;

import cn.bugstack.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.bugstack.ai.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.bugstack.ai.domain.agent.model.valobj.properties.AiAgentAutoConfigProperties;
import cn.bugstack.ai.domain.agent.service.armory.factory.DefaultArmoryFactory;
import cn.bugstack.ai.domain.session.model.entity.ChatMessageEntity;
import cn.bugstack.ai.domain.session.model.entity.ChatSessionEntity;
import cn.bugstack.ai.domain.session.model.entity.CreateSessionCommandEntity;
import cn.bugstack.ai.domain.session.service.SessionDomain;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对话服务最小实现：建会话 + 同步对话 + 流式对话。
 *
 * <p>它只负责"把一次用户输入交给已装配好的 ADK Runner 执行，并把输出落回会话"。
 * 上下文注入、RAG、工作流、运行账本这些能力属于后续阶段。</p>
 */
@Slf4j
@Service
public class ChatService implements IChatService {

    @Resource
    private DefaultArmoryFactory defaultArmoryFactory;

    @Resource
    private AiAgentAutoConfigProperties aiAgentAutoConfigProperties;

    @Resource
    private SessionDomain sessionDomain;

    @Override
    public List<AiAgentConfigTableVO.Agent> queryAiAgentConfigList() {
        if (aiAgentAutoConfigProperties.getTables() == null) {
            return List.of();
        }
        return aiAgentAutoConfigProperties.getTables().values().stream()
                .map(AiAgentConfigTableVO::getAgent)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public String createSession(String agentId, String userId) {
        AiAgentRegisterVO agent = requireAgent(agentId);

        // ADK 生成的会话 ID 同时作为平台会话 ID，避免双 ID 映射。
        Session adkSession = agent.getRunner().sessionService()
                .createSession(agent.getAppName(), userId).blockingGet();

        CreateSessionCommandEntity command = CreateSessionCommandEntity.builder()
                .sessionId(adkSession.id())
                .agentId(agentId)
                .agentName(agent.getAgentName())
                .userId(userId)
                .appName(agent.getAppName())
                .title(agent.getAgentName())
                .build();
        sessionDomain.createSession(command);
        return adkSession.id();
    }

    @Override
    public List<String> handleMessage(String agentId, String userId, String sessionId, String message) {
        AiAgentRegisterVO agent = requireAgent(agentId);
        sessionDomain.assertSessionAccess(userId, sessionId, agentId);

        // 先把用户消息落库，再调模型：模型失败时也能查到用户到底说过什么。
        sessionDomain.appendUserMessage(sessionId, message);

        Content content = Content.fromParts(Part.fromText(message));
        RunConfig runConfig = RunConfig.builder()
                .streamingMode(RunConfig.StreamingMode.SSE)
                .build();
        Flowable<Event> events = agent.getRunner().runAsync(userId, sessionId, content, runConfig,
                new ConcurrentHashMap<>());

        // ADK 事件是累计快照，用 delta 只追加新增部分。
        StringBuilder answer = new StringBuilder();
        events.blockingForEach(event -> {
            String snapshot = AgentEventTextExtractor.answerOf(event);
            answer.append(AgentEventTextExtractor.delta(answer.toString(), snapshot));
        });

        String text = answer.toString();
        sessionDomain.appendAssistantMessage(sessionId, text);
        return List.of(text);
    }

    @Override
    public Flowable<String> handleMessageStream(String agentId, String userId, String sessionId, String message) {
        AiAgentRegisterVO agent = requireAgent(agentId);
        sessionDomain.assertSessionAccess(userId, sessionId, agentId);
        sessionDomain.appendUserMessage(sessionId, message);

        Content content = Content.fromParts(Part.fromText(message));
        RunConfig runConfig = RunConfig.builder()
                .streamingMode(RunConfig.StreamingMode.SSE)
                .build();

        StringBuilder answer = new StringBuilder();
        return agent.getRunner().runAsync(userId, sessionId, content, runConfig, new ConcurrentHashMap<>())
                .map(event -> {
                    String snapshot = AgentEventTextExtractor.answerOf(event);
                    String delta = AgentEventTextExtractor.delta(answer.toString(), snapshot);
                    answer.append(delta);
                    return delta;
                })
                .filter(delta -> !delta.isEmpty())
                .doOnComplete(() -> sessionDomain.appendAssistantMessage(sessionId, answer.toString()));
    }

    @Override
    public List<ChatSessionEntity> listSessions(String userId) {
        return sessionDomain.listSessions(userId);
    }

    @Override
    public List<ChatMessageEntity> listMessages(String sessionId, String userId) {
        return sessionDomain.listMessages(sessionId, userId);
    }

    @Override
    public void deleteSession(String sessionId, String userId) {
        sessionDomain.deleteSession(sessionId, userId);
    }

    private AiAgentRegisterVO requireAgent(String agentId) {
        try {
            return defaultArmoryFactory.getAiAgentRegisterVO(agentId);
        } catch (Exception e) {
            throw new IllegalStateException("agent not assembled: " + agentId, e);
        }
    }
}
