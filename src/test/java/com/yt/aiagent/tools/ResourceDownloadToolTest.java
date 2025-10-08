package com.yt.aiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDownloadToolTest {

    @Test
    void downloadResource() {
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        String url = "https://springdoc.cn/spring-ai/_images/spring-ai-integration-diagram-3.svg";
        String fileName = "test.svg";
        String result = resourceDownloadTool.downloadResource(url, fileName);
        assertNotNull(result);
    }
}