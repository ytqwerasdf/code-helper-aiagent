package com.yt.aiagent.agent;

import com.yt.aiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * 编程助手 AI 超级智能体（拥有自足规划能力，可以直接使用）深度流式输出
 */
@Component
public class CodeManusDeepStream extends StreamToolCallAgent{

    public CodeManusDeepStream(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("CodeManusDeepStream");
        String SYSTEM_PROMPT = """
                You are codeManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                Before using each tool, clearly explain what this tool is used for(except doTerminate).
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the terminate tool/function call.
                When you finish answering, use the terminate tool/function call.
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxStep(20);
        //初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}
