package cn.bugstack.ai.api.dto;

import lombok.Data;

/**
 * 对话请求：指定 Agent、用户、会话与消息。
 */
@Data
public class ChatRequestDTO {

    private String agentId;
    private String userId;
    private String sessionId;
    private String message;
}
