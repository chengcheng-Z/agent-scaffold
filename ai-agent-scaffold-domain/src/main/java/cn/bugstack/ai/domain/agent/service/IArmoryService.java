package cn.bugstack.ai.domain.agent.service;

import cn.bugstack.ai.domain.agent.model.valobj.AiAgentConfigTableVO;

import java.util.List;

/**
 * 装配能力的对外门面：把一批 Agent 配置表装配成可执行的运行体。
 */
public interface IArmoryService {

    /**
     * 逐张配置表跑一遍完整装配链。
     *
     * @param tables 待装配的配置表
     */
    void acceptArmoryAgents(List<AiAgentConfigTableVO> tables) throws Exception;
}
