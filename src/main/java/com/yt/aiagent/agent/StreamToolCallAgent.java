package com.yt.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.yt.aiagent.agent.model.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class StreamToolCallAgent extends ReActAgent {

    //可用的工具
    private final ToolCallback[] availableTools;

    //保存工具调用信息的响应结果（要调用哪些工具）
    private ChatResponse toolCallChatResponse;

    //工具调用管理者
    private final ToolCallingManager toolCallingManager;

    //禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
    private final ChatOptions chatOptions;

    public StreamToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }


    @Override
    public boolean think() {
        return false;
    }

    @Override
    public boolean thinkWithStream(SseEmitter sseEmitter) {
        //1.校验提示词，拼接用户提示词
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }

        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, this.chatOptions);
        // 收集所有响应片段
        List<ChatResponse> allResponses = new ArrayList<>();

        log.info("开始流式调用，消息数量: {}", messageList.size());
        try {
            // 流式调用并实时输出到SSE
            Flux<ChatResponse> responseFlux = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .stream()
                    .chatResponse();

            // 处理流式响应
            responseFlux.doOnNext(response -> {
                        log.debug("收到响应: hasToolCalls={}, text={}",
                                response.hasToolCalls(),
                                response.getResult() != null ? response.getResult().getOutput().getText() : "null");
                        //收集所有响应
                        allResponses.add(response);
                        // 实时输出到SSE
                        if (response.getResult() != null && response.getResult().getOutput() != null) {
                            String text = response.getResult().getOutput().getText();
                            if (StrUtil.isNotBlank(text)) {
                                try {
                                    sseEmitter.send(text);
                                } catch (IOException e) {
                                    log.error("Failed to send SSE data", e);
                                }
                            }
                        }
                    })
                    .doOnComplete(() -> {
                        log.info("流式调用完成，共收到 {} 个响应", allResponses.size());

                        // 查找包含工具调用的响应
                        for (ChatResponse response : allResponses) {
                            if (response.hasToolCalls()){
                                this.toolCallChatResponse = response;
                                log.info("找到工具调用响应");
                                break;
                            }
                        }

                        // 如果没有工具调用，处理纯文本响应
                        if (this.toolCallChatResponse == null && !allResponses.isEmpty()) {
                            log.info("没有工具调用，处理纯文本响应");
                            // 可以选择使用最后一个响应或者抛出特定异常
                            ChatResponse lastResponse = allResponses.getLast();
                            if (lastResponse.getResult() != null && lastResponse.getResult().getOutput() != null) {
                                String text = lastResponse.getResult().getOutput().getText();
                                log.info("AI返回纯文本响应: {}", text);
                                // 将响应添加到消息列表
                                getMessageList().add(new AssistantMessage(text));
                                // 设置一个标志表示这是纯文本响应
                                this.toolCallChatResponse = lastResponse;
                            }
                        }
                    })
                    .doOnError(error -> {
                        log.error("Stream error", error);
                        try {
                            sseEmitter.send("data: [思考出错: " + error.getMessage() + "]\n\n");
                        } catch (IOException e) {
                            log.error("Failed to send error signal", e);
                        }
                    })
                    .blockLast(); // 等待完成

            if (this.toolCallChatResponse == null) {
                throw new RuntimeException("No response received");
            }

            // 解析工具调用结果
            AssistantMessage assistantMessage = this.toolCallChatResponse.getResult().getOutput();

            // 检查是否有工具调用
            if (assistantMessage.getToolCalls() == null || assistantMessage.getToolCalls().isEmpty()) {
                // 纯文本响应，没有工具调用
                String result = assistantMessage.getText();
                getResponseList().add(result);
                log.info(getName() + "返回纯文本响应，没有工具调用");
                getMessageList().add(assistantMessage);
                return false; // 表示不需要执行工具
            }

            // 有工具调用的情况
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            String result = assistantMessage.getText();
            getResponseList().add(result);
            log.info(getName() + "选择了" + toolCallList.size() + " 个工具来使用："+toolCallList.getFirst().name());

            return true; // 表示需要执行工具
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = e.getClass().getSimpleName() + ": " + e;
            }
            log.error(getName() + "的思考过程遇到了问题：" + errorMessage, e);
            getMessageList().add(new AssistantMessage("处理时遇到了错误：" + errorMessage));
            return false;
        }
    }

    @Override
    public String act() {
        if (toolCallChatResponse == null || !toolCallChatResponse.hasToolCalls()) {
            log.info("步骤{}，没有工具需要调用", getCurrentStep());
            return "没有工具需要调用";
        }
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        //调用工具
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        //记录消息上下文,conversationHistory 已经包含助手消息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        //判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> response.name().equals("doTerminate"));
        if(terminateToolCalled){
            //任务结束，更改状态
            setState(AgentState.FINISHED);
        }
        //判断是否调用的网页抓取工具
        boolean webScrapingToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> response.name().equals("scrapeWebPage"));
        //避免返回大量网页数据
        if(webScrapingToolCalled){
            String results = toolResponseMessage.getResponses().stream()
                    .map(response -> "工具 " + response.name() + " 返回结果：抓取成功")
                    .collect(Collectors.joining("\n"));
            log.info(results);
            return results;
        }

        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 返回结果： " + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);
        return results;
    }
}