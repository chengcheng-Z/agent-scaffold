package cn.bugstack.ai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话响应：返回会话号与模型输出。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseDTO {

    private String sessionId;
    private String content;
}
