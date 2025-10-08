package com.yt.aiagent.agent.model;

/**
 * 代理执行状态
 */
public enum AgentState {

    /**
     * 空闲状态
     */
    IDLE,

    /**
     * 运行中
     */
    RUNNING,

    /**
     * 已完成
     */
    FINISHED,

    /**
     * 错误状态
     */
    ERROR
}
