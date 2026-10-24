package cn.bugstack.ai.infrastructure.adapter.repository;

import cn.bugstack.ai.domain.session.adapter.repository.ISessionRepository;
import cn.bugstack.ai.domain.session.model.entity.ChatMessageEntity;
import cn.bugstack.ai.domain.session.model.entity.ChatSessionEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 会话与消息的内存仓储实现。
 *
 * <p>最小闭环用它把链路跑通；后续替换为 MySQL 实现时，只需换掉这个 Bean，领域层不动。</p>
 */
@Repository
public class InMemorySessionRepository implements ISessionRepository {

    private final Map<String, ChatSessionEntity> sessions = new ConcurrentHashMap<>();
    private final Map<String, List<ChatMessageEntity>> messages = new ConcurrentHashMap<>();

    @Override
    public void saveSession(ChatSessionEntity session) {
        sessions.put(session.getSessionId(), session);
    }

    @Override
    public ChatSessionEntity querySession(String sessionId) {
        return sessions.get(sessionId);
    }

    @Override
    public List<ChatSessionEntity> querySessionsByUser(String userId) {
        return sessions.values().stream()
                .filter(session -> userId.equals(session.getUserId()))
                .toList();
    }

    @Override
    public void deleteSession(String sessionId) {
        sessions.remove(sessionId);
        messages.remove(sessionId);
    }

    @Override
    public void saveMessage(ChatMessageEntity message) {
        messages.computeIfAbsent(message.getSessionId(), key -> new CopyOnWriteArrayList<>()).add(message);
        ChatSessionEntity session = sessions.get(message.getSessionId());
        if (session != null) {
            session.setUpdateTime(message.getCreateTime());
        }
    }

    @Override
    public List<ChatMessageEntity> queryMessages(String sessionId) {
        return messages.getOrDefault(sessionId, List.of());
    }
}
