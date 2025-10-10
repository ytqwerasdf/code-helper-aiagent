package com.yt.aiagent.tools;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.InheritedAnnotationMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class ImageSearchToolTest {

    @Resource
    ImageSearchTool imageSearchTool;

    @Test
    void searchImage() {
        String query = "芙莉莲";
        String result = imageSearchTool.searchImage(query);
        log.info(result);
        Assertions.assertNotNull(result);
    }
}