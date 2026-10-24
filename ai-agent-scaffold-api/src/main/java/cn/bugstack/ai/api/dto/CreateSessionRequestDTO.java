package cn.bugstack.ai.api.dto;

import lombok.Data;

/**
 * 建会话请求。
 */
@Data
public class CreateSessionRequestDTO {

    private String agentId;
    private String userId;
}
