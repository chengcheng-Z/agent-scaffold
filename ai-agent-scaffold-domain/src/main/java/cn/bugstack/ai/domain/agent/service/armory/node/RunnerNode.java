package cn.bugstack.ai.domain.agent.service.armory.node;

import cn.bugstack.ai.domain.agent.model.entity.ArmoryCommandEntity;
import cn.bugstack.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.bugstack.ai.domain.agent.model.valobj.AiAgentRegisterVO;
import cn.bugstack.ai.domain.agent.service.armory.AbstractArmorySupport;
import cn.bugstack.ai.domain.agent.service.armory.BoundedInMemorySessionService;
import cn.bugstack.ai.domain.agent.service.armory.factory.DefaultArmoryFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.google.adk.agents.BaseAgent;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.memory.InMemoryMemoryService;
import com.google.adk.plugins.BasePlugin;
import com.google.adk.runner.Runner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 装配链终点：把根 Agent 和插件包成 Runner，并以 agentId 为名注册进容器。
 *
 * <p>最小闭环阶段不挂插件（上下文注入、工具门禁属于后续阶段）；插件列表按空列表传入。</p>
 */
@Slf4j
@Service
public class RunnerNode extends AbstractArmorySupport {

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter,
                                        DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 装配操作 - RunnerNode");

        AiAgentConfigTableVO table = requestParameter.getAiAgentConfigTableVO();
        String appName = table.getAppName();
        AiAgentConfigTableVO.Agent agent = table.getAgent();
        String agentId = agent.getAgentId();
        String agentName = agent.getAgentName();
        String agentDesc = agent.getAgentDesc();

        AiAgentConfigTableVO.Module.Runner runnerConfig = table.getModule().getRunner();
        String rootAgentName = runnerConfig == null ? null : runnerConfig.getAgentName();
        if (StringUtils.isBlank(rootAgentName)) {
            throw new IllegalStateException("runner.agentName is null: " + appName);
        }

        BaseAgent rootAgent = dynamicContext.getAgentGroup().get(rootAgentName);
        if (rootAgent == null) {
            throw new IllegalStateException("root agent not found: " + rootAgentName);
        }

        // 最小闭环：不挂插件。后续接插件时在这里按配置解析并强制补齐安全插件。
        List<BasePlugin> plugins = new ArrayList<>();

        Runner runner = new Runner(rootAgent, appName, new InMemoryArtifactService(),
                new BoundedInMemorySessionService(), new InMemoryMemoryService(), plugins);

        AiAgentRegisterVO registerVO = AiAgentRegisterVO.builder()
                .appName(appName)
                .agentId(agentId)
                .agentName(agentName)
                .agentDesc(agentDesc)
                .orchestrationRole(agent.getOrchestrationRole())
                .allowedSubAgentIds(agent.getAllowedSubAgentIds() == null
                        ? List.of() : List.copyOf(agent.getAllowedSubAgentIds()))
                .runner(runner)
                .chatModel(dynamicContext.getChatModel())
                .build();

        // agentId 是对话时查找运行体的稳定注册键；重装配会替换旧单例。
        registerBean(agentId, AiAgentRegisterVO.class, registerVO);

        return registerVO;
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(
            ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
