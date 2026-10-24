package cn.bugstack.ai.domain.agent.model.valobj;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 一张完整的 Agent 应用配置表：把「一个智能体应用需要的全部零件」按依赖顺序描述清楚。
 *
 * <p>配置来源：application.yml（或通过 spring.config.import 引入的 agent/*.yml）里的
 * {@code ai.agent.config.tables.*}。装配链按固定顺序消费：API 客户端 → 模型 → 原子 Agent → 组合工作流 → Runner。</p>
 */
@Data
public class AiAgentConfigTableVO {

    /** ADK Runner 的应用名，决定模型侧会话数据挂在哪个应用空间下。 */
    private String appName;

    /** 这张表对外暴露的 Agent 身份信息。 */
    private Agent agent;

    /** 装配所需的全部零件。 */
    private Module module;

    /** 对外可见的 Agent 身份与展示信息。 */
    @Data
    public static class Agent {
        private String agentId;
        private String agentName;
        private String agentDesc;
        private String orchestrationRole = "NORMAL";
        private String category = "GENERAL";
        private List<String> bestFor = List.of();
        private List<String> notFor = List.of();
        private List<String> capabilities = List.of();
        private List<String> allowedSubAgentIds = List.of();
    }

    /** 装配图：这张配置表里所有可被引用的零件集合。 */
    @Data
    public static class Module {
        private AiApi aiApi;
        private ChatModel chatModel;
        private List<Agent> agents;
        private List<AgentWorkflow> agentWorkflows;
        private Runner runner;

        /** OpenAI 兼容协议的模型服务接入点。 */
        @Data
        public static class AiApi {
            private String baseUrl;
            private String apiKey;
            private String completionsPath = "/v1/chat/completions";
            private String embeddingsPath = "/v1/embeddings";
        }

        /** 聊天模型及它可使用的工具集合（最小闭环暂不装配工具）。 */
        @Data
        public static class ChatModel {
            private String model;
            private List<ToolMcp> toolMcpList;
            private List<ToolSkills> toolSkillsList;

            @Data
            public static class ToolMcp {
                private Sse sse;
                private Stdio stdio;
                private Local local;

                @Data
                public static class Sse {
                    private String name;
                    private String baseUri;
                    private String sseEndpoint;
                    private Integer requestTimeout = 3000;
                }

                @Data
                public static class Stdio {
                    private String name;
                    private Integer requestTimeout = 3000;
                    private ServerParameters serverParameters;

                    @Data
                    public static class ServerParameters {
                        private String command;
                        private List<String> args;
                        private Map<String, String> env;
                    }
                }

                @Data
                public static class Local {
                    private String name;
                }
            }

            @Data
            public static class ToolSkills {
                private String type = "directory";
                private String path;
            }
        }

        /** 一个原子 LLM Agent 的定义。 */
        @Data
        public static class Agent {
            private String name;
            private String instruction;
            private String description;
            private String outputKey;
            private String model;
            private List<ChatModel.ToolMcp> toolMcpList;
            private List<ChatModel.ToolSkills> toolSkillsList;
        }

        /** 一个组合工作流 Agent 的定义（最小闭环暂不装配）。 */
        @Data
        public static class AgentWorkflow {
            private String type;
            private String name;
            private List<String> subAgents;
            private String description;
            private Integer maxIterations = 3;
        }

        /** Runner 的装配入口。 */
        @Data
        public static class Runner {
            private String agentName;
            private List<String> pluginNameList;
        }
    }
}
