package com.yt.aiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
class CodeHelperRagCustomAdvisorFactoryTest {

    @Resource
    private VectorStore codeHelperVectorStore;

    @Test
    void createCodeHelperRagCustomAdvisor() {
        Advisor advisor = CodeHelperRagCustomAdvisorFactory.createCodeHelperRagCustomAdvisor(codeHelperVectorStore, "高级程序员");
        log.info(advisor.toString());
        Assertions.assertNotNull(advisor);

    }
}