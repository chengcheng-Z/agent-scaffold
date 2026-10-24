package cn.bugstack.ai.domain.agent.model.valobj.properties;

import cn.bugstack.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 承载 application.yml 中 {@code ai.agent.config} 这一段的静态 Agent 定义。
 *
 * <p>把「系统里有哪些 Agent、每个 Agent 用什么模型和工具」写在配置文件里，启动时一次读入内存。</p>
 */
@Data
@ConfigurationProperties(prefix = "ai.agent.config", ignoreInvalidFields = true)
public class AiAgentAutoConfigProperties {

    /** 是否在应用启动阶段就把配置表装配成可执行的 Agent。 */
    private boolean enabled = false;

    /** 配置表名称到整棵 Agent 配置树的映射。 */
    private Map<String, AiAgentConfigTableVO> tables;
}
