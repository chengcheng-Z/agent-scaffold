package cn.bugstack.ai.config;

import cn.bugstack.ai.domain.agent.model.valobj.properties.AiAgentAutoConfigProperties;
import cn.bugstack.ai.domain.agent.service.IArmoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * 启动装配入口：容器就绪后，把 application.yml 里的 Agent 配置表装配成可执行运行体。
 *
 * <p>是否装配由 {@code ai.agent.config.enabled} 控制；关闭时本地起服务不会去连模型服务。</p>
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AiAgentAutoConfigProperties.class)
public class AiAgentAutoConfig implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private AiAgentAutoConfigProperties aiAgentAutoConfigProperties;

    @Resource
    private IArmoryService armoryService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!aiAgentAutoConfigProperties.isEnabled()) {
            log.info("ai.agent.config.enabled=false，跳过 Agent 装配");
            return;
        }
        if (aiAgentAutoConfigProperties.getTables() == null || aiAgentAutoConfigProperties.getTables().isEmpty()) {
            log.info("ai.agent.config.tables 为空，跳过 Agent 装配");
            return;
        }
        try {
            log.info("Ai Agent 智能体装配开始 count:{}", aiAgentAutoConfigProperties.getTables().size());
            armoryService.acceptArmoryAgents(new ArrayList<>(aiAgentAutoConfigProperties.getTables().values()));
            log.info("Ai Agent 智能体装配完成");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
