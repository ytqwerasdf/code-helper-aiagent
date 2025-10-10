package com.yt.aiagent.tools;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistration {

    @Resource
    WebSearchTool webSearchTool;

    @Bean
    public ToolCallback[] allTools(){

        FileOperationTool fileOperationTool = new FileOperationTool();
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        TerminateTool terminateTool = new TerminateTool();
        PDFWithImagesTool pdfWithImagesTool = new PDFWithImagesTool();
        SearchAndDownloadTool searchAndDownloadTool = new SearchAndDownloadTool();
        ImageSearchTool imageSearchTool = new ImageSearchTool();

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
                imageSearchTool
        );
    }
}
