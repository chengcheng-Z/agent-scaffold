package cn.bugstack.ai.domain.session.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 建会话命令。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSessionCommandEntity {

    private String sessionId;
    private String agentId;
    private String agentName;
    private String userId;
    private String appName;
    private String title;
}
