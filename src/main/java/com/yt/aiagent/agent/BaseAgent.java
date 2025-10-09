package com.yt.aiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.yt.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程
 *
 * 提供状态转换，内存管理和基于步骤的执行循环的基础功能
 * 子类必须实现step方法
 */
@Data
@Slf4j
public abstract class BaseAgent {
    //核心属性
    private String name;

    //提示词
    private String systemPrompt;
    private String nextStepPrompt;

    //代理状态
    private AgentState state = AgentState.IDLE;

    //执行步骤控制
    private int currentStep = 0;
    private int maxStep = 10;

    //LLM大模型
    private ChatClient chatClient;

    //Memory 记忆（需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     * @param userPrompt
     * @return
     */
    public String run(String userPrompt){
        if (this.state != AgentState.IDLE){
            throw new RuntimeException("Cannot run agent from state: "+this.state);
        }
        if(StrUtil.isBlank(userPrompt)){
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        //更改状态
        this.state = AgentState.RUNNING;
        //记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        //保存结果列表
        List<String> results = new ArrayList<>();
        try {
            //执行循环
            for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                int stepNumber = i+1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}",stepNumber,maxStep);
                //单步执行
                String stepResult = step();
                String result = "Step" + stepNumber + ": "+stepResult;
                results.add(result);
            }
            //检查是否超出步骤限制
            if(currentStep >= maxStep) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxStep + ")");
            }
            return String.join("\n",results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("error executing agent",e);
            return "执行错误"+e.getMessage();
        }finally {
            //清理资源
            this.cleanUp();
        }
    }

    /**
     * 运行代理(异步)
     * @param userPrompt
     * @return
     */
    public SseEmitter runStream(String userPrompt){

        SseEmitter sseEmitter = new SseEmitter(300000L);
        //使用线程异步处理
        CompletableFuture.runAsync(() -> {
            try {
                //基础校验
                if (this.state != AgentState.IDLE){
                    sseEmitter.send("无法从该状态运行代理: "+this.state);
                    sseEmitter.complete();
                    return;
                }
                if(StrUtil.isBlank(userPrompt)){
                    sseEmitter.send("不能使用空提示词运行代理 ");
                    sseEmitter.complete();
                    return;
                }
            }catch (Exception e){
                sseEmitter.completeWithError(e);
            }
            //更改状态
            this.state = AgentState.RUNNING;
            //记录消息上下文
            messageList.add(new UserMessage(userPrompt));
            //保存结果列表
            List<String> results = new ArrayList<>();
            try {
                //执行循环
                for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                    int stepNumber = i+1;
                    currentStep = stepNumber;
                    log.info("Executing step {}/{}",stepNumber,maxStep);
                    //单步执行
                    String stepResult = step();
                    String result = "Step" + stepNumber + ": "+stepResult;
                    results.add(result);
                    //输出当前结果到SSE
                    sseEmitter.send(result);
                }
                //检查是否超出步骤限制
                if(currentStep >= maxStep) {
                    state = AgentState.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxStep + ")");
                    sseEmitter.send("Terminated: Reached max steps (" + maxStep + ")");
                }
                //正常完成
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("error executing agent",e);
                try {
                    sseEmitter.send("执行错误"+e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
//                sseEmitter.completeWithError();
            }finally {
                //清理资源
                this.cleanUp();
            }
        });

        //设置超时回调
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanUp();
            log.warn("SSE connection timeout");
        });
        //设置完成回调
        sseEmitter.onCompletion(() ->{
            if(this.state == AgentState.RUNNING){
                this.state = AgentState.FINISHED;
            }
            this.cleanUp();
            log.info("SSE connection completed");
        });

        return sseEmitter;
    }

    /**
     * 定义单个步骤
     * @return
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanUp(){
        //子类重写此方法清理资源
    }
}
