package cn.bugstack.ai.domain.agent.model.valobj;

import com.google.adk.runner.Runner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;

/**
 * 一个 Agent 装配完成后的运行时句柄：身份信息 + 可以直接执行的 ADK Runner。
 *
 * <p>装配链跑完后打包成这个对象，按 agentId 注册进 Spring 容器；对话时按 agentId 取出来直接用。</p>
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAgentRegisterVO {

    private String appName;
    private String agentId;
    private String agentName;
    private String agentDesc;
    private String orchestrationRole;
    private List<String> allowedSubAgentIds;

    /** 已经把模型、工具、子 Agent 全部装好的 ADK 执行器。 */
    private Runner runner;

    /** 这个 Agent 使用的 Spring AI 聊天模型（上下文压缩等也会复用）。 */
    private ChatModel chatModel;
}
