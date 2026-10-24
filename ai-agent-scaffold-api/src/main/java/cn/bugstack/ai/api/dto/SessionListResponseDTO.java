package cn.bugstack.ai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话列表项。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionListResponseDTO {

    private String sessionId;
    private String agentId;
    private String agentName;
    private String userId;
    private String title;
    private String createTime;
}
