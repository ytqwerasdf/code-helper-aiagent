package com.yt.aiagent.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class SseExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(SseExceptionHandler.class);
    
    @ExceptionHandler(IOException.class)
    public void handleIOException(IOException ex, HttpServletRequest request) {
        // 只记录警告，不打印堆栈
        if (ex.getMessage() != null && 
            (ex.getMessage().contains("你的主机中的软件中止了一个已建立的连接") ||
             ex.getMessage().contains("Connection reset by peer") ||
             ex.getMessage().contains("Broken pipe"))) {
            log.warn("SSE client disconnected: {}", ex.getMessage());
        } else {
            log.error("Unexpected IOException", ex);
        }
    }
}