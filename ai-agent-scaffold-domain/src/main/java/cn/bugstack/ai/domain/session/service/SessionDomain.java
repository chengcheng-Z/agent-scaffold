package cn.bugstack.ai.domain.session.service;

import cn.bugstack.ai.domain.session.adapter.repository.ISessionRepository;
import cn.bugstack.ai.domain.session.model.entity.ChatMessageEntity;
import cn.bugstack.ai.domain.session.model.entity.ChatSessionEntity;
import cn.bugstack.ai.domain.session.model.entity.CreateSessionCommandEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 会话领域服务：负责会话创建、归属校验与消息读写。
 */
@Service
public class SessionDomain {

    private final ISessionRepository sessionRepository;

    public SessionDomain(ISessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /** 建会话：sessionId 由调用方传入（复用 ADK 会话 ID）。 */
    public String createSession(CreateSessionCommandEntity command) {
        LocalDateTime now = LocalDateTime.now();
        ChatSessionEntity session = ChatSessionEntity.builder()
                .sessionId(command.getSessionId())
                .agentId(command.getAgentId())
                .agentName(command.getAgentName())
                .userId(command.getUserId())
                .appName(command.getAppName())
                .title(command.getTitle())
                .createTime(now)
                .updateTime(now)
                .build();
        sessionRepository.saveSession(session);
        return session.getSessionId();
    }

    /** 取会话，不存在直接报错。 */
    public ChatSessionEntity requireSession(String sessionId) {
        ChatSessionEntity session = sessionRepository.querySession(sessionId);
        if (session == null) {
            throw new IllegalStateException("session not found: " + sessionId);
        }
        return session;
    }

    /** 校验会话归属：会话必须是这个用户的，且属于这个 Agent（agentId 为空时跳过）。 */
    public void assertSessionAccess(String userId, String sessionId, String agentId) {
        ChatSessionEntity session = requireSession(sessionId);
        if (!session.getUserId().equals(userId)) {
            throw new IllegalStateException("session does not belong to user: " + sessionId);
        }
        if (agentId != null && !agentId.equals(session.getAgentId())) {
            throw new IllegalStateException("session does not belong to agent: " + sessionId);
        }
    }

    public ChatMessageEntity appendUserMessage(String sessionId, String content) {
        return appendMessage(sessionId, "user", content);
    }

    public ChatMessageEntity appendAssistantMessage(String sessionId, String content) {
        return appendMessage(sessionId, "assistant", content);
    }

    private ChatMessageEntity appendMessage(String sessionId, String role, String content) {
        ChatMessageEntity message = ChatMessageEntity.builder()
                .messageId(UUID.randomUUID().toString())
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .createTime(LocalDateTime.now())
                .build();
        sessionRepository.saveMessage(message);
        return message;
    }

    public List<ChatSessionEntity> listSessions(String userId) {
        return sessionRepository.querySessionsByUser(userId).stream()
                .sorted(Comparator.comparing(ChatSessionEntity::getUpdateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<ChatMessageEntity> listMessages(String sessionId, String userId) {
        assertSessionAccess(userId, sessionId, null);
        return sessionRepository.queryMessages(sessionId).stream()
                .sorted(Comparator.comparing(ChatMessageEntity::getCreateTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public void deleteSession(String sessionId, String userId) {
        assertSessionAccess(userId, sessionId, null);
        sessionRepository.deleteSession(sessionId);
    }
}
