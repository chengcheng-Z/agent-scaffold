package cn.bugstack.ai.domain.session.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话中的一条消息。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageEntity {

    private String messageId;
    private String sessionId;
    /** user / assistant */
    private String role;
    private String content;
    private LocalDateTime createTime;
}
