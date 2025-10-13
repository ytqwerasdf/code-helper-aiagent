package com.yt.aiagent.agent;

import com.yt.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * ReAct(Reasoning and Acting)模式的代理抽象类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {
    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 处理当前状态并决定下一步行动(思考期间流式输出)
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean thinkWithStream(SseEmitter sseEmitter);

    /**
     * 执行决定的行动
     *
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤，思考和行动
     *
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            //先思考
            boolean shouldAct = think();
            if (!shouldAct) {
                if (shouldEndConversation()) {
                    setState(AgentState.FINISHED);
                    return "思考完成 - 无需行动,会话结束";
                }
                return "思考完成 - 无需行动";
            }
            //再行动
            return act();
        } catch (Exception e) {
            //记录异常日志
            e.printStackTrace();
            return "步骤执行失败：" + e.getMessage();
        }
    }

    @Override
    public String stepWithStream(SseEmitter sseEmitter){
        try {
            //先思考
            boolean shouldAct = thinkWithStream(sseEmitter);
            if (!shouldAct) {
                if (shouldEndConversation()) {
                    setState(AgentState.FINISHED);
                    return "思考完成 - 无需调用工具";
                }
                return "思考完成 - 无需调用工具";
            }
            //再行动
            return act();
        } catch (Exception e) {
            //记录异常日志
            log.error("step步骤执行失败："+e.getMessage());
            return "步骤执行失败：" + e.getMessage();
        }
    }

    /**
     * 判断是否应该结束会话
     */
    private boolean shouldEndConversation() {
        // 检查最后一条助手消息
        if (!getMessageList().isEmpty()) {
            Message lastMessage = getMessageList().getLast();
            if (lastMessage instanceof AssistantMessage) {
                String content = lastMessage.getText();
                // 如果是简单的问候回复，可以结束
                return content.contains("你好") || content.contains("Hello") || content.contains("Let me know how") || content.contains("letme") ||
                        content.contains("有什么可以帮助") || content.contains("How can I help");
            }
        }
        return false;
    }
}
