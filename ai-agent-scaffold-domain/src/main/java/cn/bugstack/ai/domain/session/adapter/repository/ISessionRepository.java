package cn.bugstack.ai.domain.session.adapter.repository;

import cn.bugstack.ai.domain.session.model.entity.ChatMessageEntity;
import cn.bugstack.ai.domain.session.model.entity.ChatSessionEntity;

import java.util.List;

/**
 * 会话与消息的存储端口。最小闭环用内存实现，后续可替换为 MySQL。
 */
public interface ISessionRepository {

    void saveSession(ChatSessionEntity session);

    ChatSessionEntity querySession(String sessionId);

    List<ChatSessionEntity> querySessionsByUser(String userId);

    void deleteSession(String sessionId);

    void saveMessage(ChatMessageEntity message);

    List<ChatMessageEntity> queryMessages(String sessionId);
}
