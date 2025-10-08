package com.yt.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class CodeHelperAppTest {

    @Resource
    CodeHelperApp codeHelperApp;
    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        //第一轮
        String message = "你好，我是java练习生，练习时长两年半";
        String answer = codeHelperApp.doChat(message,chatId);
        Assertions.assertNotNull(answer);

        //第二轮
         message = "请帮我写一段约瑟夫环问题的解法";
        answer = String.valueOf(codeHelperApp.doChatWithReport(message,chatId));
        Assertions.assertNotNull(answer);
        //第三轮
         message = "我之前提的问题是什么？帮我回忆一下";
        answer = String.valueOf(codeHelperApp.doChatWithReport(message,chatId));
        Assertions.assertNotNull(answer);

    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        //第一轮
        String message = "你好，我是java练习生，练习时长两年半";
        CodeHelperApp.CodeReport answer = codeHelperApp.doChatWithReport(message,chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        //第一轮
        String message = "并发编程的注意事项有哪些？";
        String answer = codeHelperApp.doChatWithRag(message,chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
         //测试联网搜索问题的答案
        testMessage("重庆邮电大学是211吗");
        //测试网页抓取
        testMessage("最近想学ai编程了，看看spring官方网站（springdoc.cn）对springai的介绍是怎样的");
        //测试资源下载
        testMessage("下载一张我最喜欢的芙莉莲的图片");
        //测试终端操作，执行代码
        testMessage("执行 Python3 脚本生成一份数据分析报告，如果尝试三次不成功就放弃");
        //测试文件操作，保存用户档案
        testMessage("保存我的历史对话为文件");
        //测试pdf生成
        testMessage("生成一份'上海漫展游玩计划'PDF，包含游玩时间、地点等");
    }
    private void testMessage(String message){
        String chatId = UUID.randomUUID().toString();
        String result = codeHelperApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(result);
    }

    @Test
    void doChatWithMCP() {
        String chatId = UUID.randomUUID().toString();
        //测试地图MCP
//        String message = "重庆今天的天气如何？重庆邮电大学附近什么地方适合约会？";
//        String result = codeHelperApp.doChatWithMCP(message, chatId);
//        Assertions.assertNotNull(result);
        //测试本地开发的图片搜索MCP
        String message = "帮我搜索电脑的图片";
        String result = codeHelperApp.doChatWithMCP(message, chatId);
        Assertions.assertNotNull(result);
    }
}