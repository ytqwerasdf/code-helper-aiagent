package com.yt.aiagent.controller;

import com.yt.aiagent.agent.CodeManus;
import com.yt.aiagent.agent.CodeManusDeepStream;
import com.yt.aiagent.app.CodeHelperApp;
import com.yt.aiagent.constant.ConversationSign;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private CodeHelperApp codeHelperApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;
    @Autowired
    private RetryTemplate retryTemplate;

    @GetMapping("/health")
    public boolean healthCheck(){return true;}

    /**
     * 同步调用编程助手应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/code_helper/chat/sync")
    public String doChatWithCodeAppSync(String message,String chatId){
        return codeHelperApp.doChat(message,chatId);
    }

    /**
     * SSE流式调用编程助手应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/code_helper/chat/sse",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithCodeAppSSE(String message, String chatId){
        // 业务流
        Flux<String> dataStream = codeHelperApp.doChatByStream(message, chatId)
                // 正常完成追加结束标识
                .concatWith(Mono.just(ConversationSign.CONVERSATION_END));

        // 心跳：注释行，防止中间网络设备超时断开
        Flux<String> heartbeat = Flux.interval(Duration.ofSeconds(15))
                .map(t -> ": keepalive\n\n")
                .takeUntilOther(dataStream.ignoreElements().then());

        return Flux.merge(dataStream, heartbeat);
    }
    /**
     * SSE流式调用编程助手应用(使用RAG)
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/code_helper/chat/sse/rag",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatRagWithCodeAppSSE(String message, String chatId){
        // 业务流
        Flux<String> dataStream = codeHelperApp.doChatWithRag(message, chatId)
                // 正常完成追加结束标识
                .concatWith(Mono.just(ConversationSign.CONVERSATION_END));

        // 心跳：注释行，防止中间网络设备超时断开
        Flux<String> heartbeat = Flux.interval(Duration.ofSeconds(15))
                .map(t -> ": keepalive\n\n")
                .takeUntilOther(dataStream.ignoreElements().then());

        return Flux.merge(dataStream, heartbeat);
    }

    /**
     * SSE流式调用编程助手应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/code_helper/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithCodeAppServerSentEvent(String message, String chatId){
        return codeHelperApp.doChatByStream( message,chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * SSE流式调用编程助手应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/code_helper/chat/sse_emitter")
    public SseEmitter doChatWithCodeAppSseEmitter(String message, String chatId){
        SseEmitter sseEmitter = new SseEmitter(180000L);//超时时间3分钟

        //获取Flux响应式数据流并且直接通过订阅推送给前端
        codeHelperApp.doChatByStream( message,chatId)
                 .subscribe(chunk -> {
                     try {
                         sseEmitter.send(chunk);
                     } catch (IOException e) {
                         sseEmitter.completeWithError(e);
                     }
                 },sseEmitter::completeWithError,sseEmitter::complete);
        return sseEmitter;
    }


    /**
     * 流式调用 AI 智能体Manus
     *
     * @param message
     * @return
     */
    @GetMapping(value = "/manus/chat",produces = "text/event-stream;charset=UTF-8")
    public SseEmitter doChatWithManus(String message){
        CodeManus codeManus = new CodeManus(allTools, dashscopeChatModel);
        CodeManusDeepStream codeManusDeepStream = new CodeManusDeepStream(allTools,dashscopeChatModel);
        return codeManusDeepStream.runStreamDeep(message);
//        return codeManus.runStream(message);

    }

}
