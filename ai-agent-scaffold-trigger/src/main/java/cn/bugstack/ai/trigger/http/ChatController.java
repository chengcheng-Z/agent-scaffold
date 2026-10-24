package cn.bugstack.ai.trigger.http;

import cn.bugstack.ai.api.dto.ChatRequestDTO;
import cn.bugstack.ai.api.dto.ChatResponseDTO;
import cn.bugstack.ai.api.response.Response;
import cn.bugstack.ai.domain.agent.service.IChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 对话入口：同步对话 + SSE 流式对话。
 */
@RestController
@RequestMapping("/api/v1")
public class ChatController {

    private final IChatService chatService;

    public ChatController(IChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public Response<ChatResponseDTO> chat(@RequestBody ChatRequestDTO request) {
        try {
            String userId = defaultUser(request.getUserId());
            String sessionId = ensureSession(request, userId);
            List<String> outputs = chatService.handleMessage(request.getAgentId(), userId, sessionId,
                    request.getMessage());
            String content = outputs.isEmpty() ? "" : outputs.get(0);
            return success(ChatResponseDTO.builder().sessionId(sessionId).content(content).build());
        } catch (Exception e) {
            return failure(e);
        }
    }

    @PostMapping(value = "/chat_stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody ChatRequestDTO request) {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            String userId = defaultUser(request.getUserId());
            String sessionId = ensureSession(request, userId);

            // 先把会话号推给前端，后续每轮都带着它续聊。
            emitter.send(SseEmitter.event().name("session").data(sessionId));

            chatService.handleMessageStream(request.getAgentId(), userId, sessionId, request.getMessage())
                    .subscribe(
                            delta -> {
                                try {
                                    emitter.send(SseEmitter.event().name("message").data(delta));
                                } catch (Exception sendError) {
                                    emitter.completeWithError(sendError);
                                }
                            },
                            emitter::completeWithError,
                            emitter::complete);
            return emitter;
        } catch (Exception e) {
            emitter.completeWithError(e);
            return emitter;
        }
    }

    private String ensureSession(ChatRequestDTO request, String userId) {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            return chatService.createSession(request.getAgentId(), userId);
        }
        return sessionId;
    }

    private String defaultUser(String userId) {
        return userId == null || userId.isBlank() ? "anonymous" : userId;
    }

    private <T> Response<T> success(T data) {
        return Response.<T>builder().code("0000").info("成功").data(data).build();
    }

    private <T> Response<T> failure(Exception e) {
        return Response.<T>builder().code("5000").info(e.getMessage()).build();
    }
}
