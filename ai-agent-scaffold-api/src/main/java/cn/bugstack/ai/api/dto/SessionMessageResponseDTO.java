package cn.bugstack.ai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话消息项。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionMessageResponseDTO {

    private String messageId;
    private String role;
    private String content;
    private String createTime;
}
