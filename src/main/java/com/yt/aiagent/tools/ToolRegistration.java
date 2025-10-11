package com.yt.aiagent.tools;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistration {

    @Resource
    FileOperationTool fileOperationTool;

    @Resource
    ImageSearchTool imageSearchTool;

    @Resource
    PDFGenerationTool pdfGenerationTool;

    @Resource
    PDFWithImagesTool pdfWithImagesTool;

    @Resource
    ResourceDownloadTool resourceDownloadTool;

    @Resource
    SearchAndDownloadTool searchAndDownloadTool;

    @Resource
    TerminalOperationTool terminalOperationTool;

    @Resource
    TerminateTool terminateTool;

    @Resource
    WebScrapingTool webScrapingTool;

    @Resource
    WebSearchTool webSearchTool;

    @Resource
    LangSearchTool langSearchTool;


    @Bean
    public ToolCallback[] allTools(){
        return ToolCallbacks.from(
                fileOperationTool,
                webSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool,
                terminateTool,
                pdfWithImagesTool,
                searchAndDownloadTool,
                imageSearchTool,
                langSearchTool
        );
    }
}
