package cn.bugstack.ai.domain.session.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 平台会话：一次多轮对话的归属。sessionId 与 ADK 侧会话保持同一个，避免双 ID 映射。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatSessionEntity {

    private String sessionId;
    private String agentId;
    private String agentName;
    private String userId;
    private String appName;
    private String title;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
