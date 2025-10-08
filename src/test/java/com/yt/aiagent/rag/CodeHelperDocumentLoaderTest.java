package com.yt.aiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CodeHelperDocumentLoaderTest {

    @Resource
    private CodeHelperDocumentLoader codeHelperDocumentLoader;

    @Test
    public void test(){
        codeHelperDocumentLoader.loadMarkdowns();
    }
}