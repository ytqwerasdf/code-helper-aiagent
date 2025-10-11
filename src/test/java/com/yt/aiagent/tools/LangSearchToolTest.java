package com.yt.aiagent.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LangSearchToolTest {

    @Resource
    LangSearchTool langSearchTool;

    @Test
    void searchWeb() {
    }

    @Test
    void searchWebSimple() {
        String result = langSearchTool.searchWebSimple("重庆邮电大学");
        Assertions.assertNotNull(result);
    }
}