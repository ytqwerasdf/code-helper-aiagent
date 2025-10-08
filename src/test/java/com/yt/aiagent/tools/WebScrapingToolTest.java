package com.yt.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapingToolTest {

    @Test
    void scrapeWebPage() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String webPage = webScrapingTool.scrapeWebPage("https://springdoc.cn/spring-ai/");
        Assertions.assertNotNull(webPage);
    }
}