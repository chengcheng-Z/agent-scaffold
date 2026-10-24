package cn.bugstack.ai.domain.agent.service.armory.factory;

import cn.bugstack.ai.domain.agent.model.entity.ArmoryCommandEntity;
import cn.bugstack.ai.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.bugstack.ai.domain.agent.service.armory.node.RootNode;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.google.adk.agents.BaseAgent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 装配链的入口工厂，同时定义链上共享的「工作台」上下文。
 */
@Service
public class DefaultArmoryFactory {

    /** Spring 容器，同时充当「已装配 Agent 运行体」的运行时注册表。 */
    @Resource
    private ApplicationContext applicationContext;

    /** 装配责任链的固定起点。 */
    @Resource
    private RootNode rootNode;

    /** 取出装配责任链的入口节点。 */
    public StrategyHandler<ArmoryCommandEntity, DynamicContext, AiAgentRegisterVO> armoryStrategyHandler() {
        return rootNode;
    }

    /** 按 agentId 取出已经装配完成并注册进容器的运行体。 */
    public AiAgentRegisterVO getAiAgentRegisterVO(String agentId) {
        return applicationContext.getBean(agentId, AiAgentRegisterVO.class);
    }

    /**
     * 一次装配过程中的共享工作台，只在单张配置表的责任链执行期间有效。
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        /** 底层 HTTP 客户端：后面所有模型复用同一个，连接池只有一份。 */
        private OpenAiApi openAiApi;

        /** 这张配置表的默认聊天模型实例。 */
        private ChatModel chatModel;

        /** 默认模型的模型代码。 */
        private String chatModelName;

        /** 本次装配已经建好的 Agent 名称索引。 */
        @Builder.Default
        private Map<String, BaseAgent> agentGroup = new HashMap<>();

        /** 当前正在装配第几条组合工作流配置（最小闭环暂不使用）。 */
        @Builder.Default
        private AtomicInteger currentStepIndex = new AtomicInteger(0);

        /** 跨节点传递的扩展数据。 */
        @Builder.Default
        private Map<String, Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key, T value) {
            dataObjects.put(key, value);
        }

        public <T> T getValue(String key) {
            return (T) dataObjects.get(key);
        }

        /** 把配置里的子 Agent 名字解析成已装配好的 Agent 实例列表。 */
        public List<BaseAgent> queryAgentList(List<String> agentNames) {
            if (agentNames == null || agentNames.isEmpty() || agentGroup == null) {
                return Collections.emptyList();
            }
            return agentNames.stream().map(agentGroup::get).filter(java.util.Objects::nonNull).toList();
        }

        public int getCurrentStepIndex() {
            return currentStepIndex.get();
        }

        public void addCurrentStepIndex() {
            currentStepIndex.incrementAndGet();
        }
    }
}
