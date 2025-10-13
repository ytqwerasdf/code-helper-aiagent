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
            for (Message message : getMessageList()) {
                return isSimpleGreeting(message.getText());
            }
        }
        return false;
    }

    private static boolean isSimpleGreeting(String raw) {
        if (raw == null) return false;

        // 1) 规范化：小写、去除所有空白、去除标点/特殊符号（保留中英文）
        String norm = raw.toLowerCase()
                .replaceAll("\\s+", "")                 // 去空白
                .replaceAll("[\\p{Punct}\\p{IsPunctuation}]+", ""); // 去标点

        // 2) 快速关键词（中文）
        if (norm.contains("你好") || norm.contains("您好") || norm.contains("有什么可以帮助") || norm.contains("需要什么帮助")) {
            return true;
        }

        // 3) 英文关键词（考虑粘连/无空格场景）
        // hi/hello/hey
        if (norm.contains("hi") || norm.contains("hello") || norm.contains("hey")) return true;

        // howcan i help/assist you / how can i help
        if (norm.contains("howcanihelp") || norm.contains("howcaniassist") || norm.contains("howcanihelpyou") || norm.contains("howcaniassistyou"))
            return true;

        // let me know how ... (常见模板的压缩形态)
        if (norm.contains("letmeknowhow")) return true;

        // i'm here to help / im here to help（考虑省略撇号）
        if (norm.contains("imheretohelp") || norm.contains("iamheretohelp")) return true;

        // Manus/系统开场白（你示例中常见）
        if (norm.contains("immanus") || norm.contains("allcapableaiassistant") || norm.contains("iamhere tohelp".replace(" ", "")))
            return true;

        return false;
    }
}
