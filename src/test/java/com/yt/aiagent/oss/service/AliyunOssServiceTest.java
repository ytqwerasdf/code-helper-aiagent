package com.yt.aiagent.oss.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AliyunOssServiceTest {

    @Resource
    AliyunOssService aliyunOssService;

    @Test
    void upLoad() {
        String upLoad = aliyunOssService.upLoad("D:\\34978\\javaEE\\ai-agent\\tmp\\file\\date_plan.txt");
        Assertions.assertNotNull(upLoad);
    }

    @Test
    void downLoad() {
    }

    @Test
    void listFile() {
    }

    @Test
    void delete() {
    }
}