package cn.bugstack.ai.domain.agent.model.entity;

import cn.bugstack.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 装配命令：把「要装配哪一张 Agent 配置表」包成一个对象，在装配链的各个节点之间传递。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArmoryCommandEntity {

    /** 本次要装配的整棵配置树。 */
    private AiAgentConfigTableVO aiAgentConfigTableVO;
}
