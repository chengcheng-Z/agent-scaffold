package cn.bugstack.ai.trigger.http;

import cn.bugstack.ai.api.dto.CreateSessionRequestDTO;
import cn.bugstack.ai.api.dto.CreateSessionResponseDTO;
import cn.bugstack.ai.api.dto.SessionListResponseDTO;
import cn.bugstack.ai.api.dto.SessionMessageResponseDTO;
import cn.bugstack.ai.api.response.Response;
import cn.bugstack.ai.domain.agent.service.IChatService;
import cn.bugstack.ai.domain.session.model.entity.ChatMessageEntity;
import cn.bugstack.ai.domain.session.model.entity.ChatSessionEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会话管理入口：建会话、列会话、列消息、删会话。
 */
@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final IChatService chatService;

    public SessionController(IChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Response<CreateSessionResponseDTO> create(@RequestBody CreateSessionRequestDTO request) {
        try {
            String userId = defaultUser(request.getUserId());
            String sessionId = chatService.createSession(request.getAgentId(), userId);
            return success(CreateSessionResponseDTO.builder().sessionId(sessionId).build());
        } catch (Exception e) {
            return failure(e);
        }
    }

    @GetMapping
    public Response<List<SessionListResponseDTO>> list(
            @RequestParam(value = "userId", required = false) String userId) {
        try {
            List<SessionListResponseDTO> list = chatService.listSessions(defaultUser(userId)).stream()
                    .map(this::toSessionDTO)
                    .toList();
            return success(list);
        } catch (Exception e) {
            return failure(e);
        }
    }

    @GetMapping("/{sessionId}/messages")
    public Response<List<SessionMessageResponseDTO>> messages(@PathVariable String sessionId,
                                                             @RequestParam(value = "userId", required = false) String userId) {
        try {
            List<SessionMessageResponseDTO> list = chatService.listMessages(sessionId, defaultUser(userId)).stream()
                    .map(this::toMessageDTO)
                    .toList();
            return success(list);
        } catch (Exception e) {
            return failure(e);
        }
    }

    @DeleteMapping("/{sessionId}")
    public Response<Void> delete(@PathVariable String sessionId,
                                 @RequestParam(value = "userId", required = false) String userId) {
        try {
            chatService.deleteSession(sessionId, defaultUser(userId));
            return success(null);
        } catch (Exception e) {
            return failure(e);
        }
    }

    private SessionListResponseDTO toSessionDTO(ChatSessionEntity session) {
        return SessionListResponseDTO.builder()
                .sessionId(session.getSessionId())
                .agentId(session.getAgentId())
                .agentName(session.getAgentName())
                .userId(session.getUserId())
                .title(session.getTitle())
                .createTime(session.getCreateTime() == null ? null : session.getCreateTime().toString())
                .build();
    }

    private SessionMessageResponseDTO toMessageDTO(ChatMessageEntity message) {
        return SessionMessageResponseDTO.builder()
                .messageId(message.getMessageId())
                .role(message.getRole())
                .content(message.getContent())
                .createTime(message.getCreateTime() == null ? null : message.getCreateTime().toString())
                .build();
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
