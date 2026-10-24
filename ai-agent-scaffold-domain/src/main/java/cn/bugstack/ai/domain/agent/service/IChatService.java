package cn.bugstack.ai.domain.agent.service;

import cn.bugstack.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.bugstack.ai.domain.session.model.entity.ChatMessageEntity;
import cn.bugstack.ai.domain.session.model.entity.ChatSessionEntity;
import io.reactivex.rxjava3.core.Flowable;

import java.util.List;

/**
 * 对话领域的唯一入口：建会话、发消息、拿流全走这里。
 */
public interface IChatService {

    /** 列出当前配置里可见的 Agent。 */
    List<AiAgentConfigTableVO.Agent> queryAiAgentConfigList();

    /** 为一个 Agent 建立新会话，返回 sessionId。 */
    String createSession(String agentId, String userId);

    /** 在已有会话里同步跑一轮对话，返回模型输出。 */
    List<String> handleMessage(String agentId, String userId, String sessionId, String message);

    /** 在已有会话里流式跑一轮对话，返回增量文本流。 */
    Flowable<String> handleMessageStream(String agentId, String userId, String sessionId, String message);

    /** 列出某用户的会话。 */
    List<ChatSessionEntity> listSessions(String userId);

    /** 列出某会话的消息。 */
    List<ChatMessageEntity> listMessages(String sessionId, String userId);

    /** 删除某会话。 */
    void deleteSession(String sessionId, String userId);
}
