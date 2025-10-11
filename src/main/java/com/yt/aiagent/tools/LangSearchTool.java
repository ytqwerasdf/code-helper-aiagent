package com.yt.aiagent.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LangSearch 网页搜索工具
 * 使用 LangSearch API 进行网页搜索
 */
@Component
public class LangSearchTool {
    
    private static final String LANGSEARCH_API_URL = "https://api.langsearch.com/v1/web-search";
    private static final Logger log = LoggerFactory.getLogger(LangSearchTool.class);

    @Value("${langsearch.api-key}")
    private String apiKey;

    @Tool(description = "Search for information from Internet with enhanced results and summaries")
    public String searchWeb(
            @ToolParam(description = "Search query keyword") String query,
            @ToolParam(description = "Time range for search results (oneDay, oneWeek, oneMonth, oneYear, noLimit)") String freshness,
            @ToolParam(description = "Whether to include detailed summaries") Boolean summary,
            @ToolParam(description = "Number of results to return (1-10)") Integer count) {
        
        try {
            // 设置默认值
            if (freshness == null) freshness = "noLimit";
            if (summary == null) summary = true;
            if (count == null) count = 5;
            
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("query", query);
            requestBody.put("freshness", freshness);
            requestBody.put("summary", summary);
            requestBody.put("count", count);
            
            // 发送 POST 请求
            HttpResponse response = HttpRequest.post(LANGSEARCH_API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .execute();
            
            if (response.getStatus() != 200) {
                return "Error: HTTP " + response.getStatus() + " - " + response.body();
            }
            
            // 解析响应
            JSONObject jsonResponse = JSONUtil.parseObj(response.body());
            JSONObject data = jsonResponse.getJSONObject("data");
            
            if (data == null) {
                return "Error: No data in response";
            }
            
            JSONObject webPages = data.getJSONObject("webPages");
            if (webPages == null) {
                return "No search results found";
            }
            
            JSONArray results = webPages.getJSONArray("value");
            if (results == null || results.isEmpty()) {
                return "No search results found";
            }
            
            // 格式化结果
            List<String> formattedResults = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                JSONObject item = results.getJSONObject(i);
                StringBuilder result = new StringBuilder();
                
                result.append("Title: ").append(item.getStr("name", "N/A")).append("\n");
                result.append("URL: ").append(item.getStr("url", "N/A")).append("\n");
                result.append("Snippet: ").append(item.getStr("snippet", "N/A")).append("\n");
                
                // 如果有详细摘要，添加摘要信息
                if (summary && item.containsKey("summary")) {
                    String summaryText = item.getStr("summary");
                    if (summaryText != null && !summaryText.isEmpty()) {
                        // 截取摘要的前500个字符，避免过长
                        String shortSummary = summaryText.length() > 500 
                            ? summaryText.substring(0, 500) + "..." 
                            : summaryText;
                        result.append("Summary: ").append(shortSummary).append("\n");
                    }
                }
                
                // 添加发布日期信息
                if (item.containsKey("datePublished") && item.getStr("datePublished") != null) {
                    result.append("Published: ").append(item.getStr("datePublished")).append("\n");
                }
                
                result.append("\n");
                formattedResults.add(result.toString());
            }
            
            String finalResult = formattedResults.stream()
                    .collect(Collectors.joining());
            
            log.info("LangSearch results for query '{}': {} results found", query, results.size());
            return finalResult;
            
        } catch (Exception e) {
            log.error("Error searching with LangSearch: {}", e.getMessage(), e);
            return "Error searching with LangSearch: " + e.getMessage();
        }
    }
    
    /**
     * 简化版本的搜索方法，使用默认参数
     */
    @Tool(description = "Search for information from Internet with default settings")
    public String searchWebSimple(@ToolParam(description = "Search query keyword") String query) {
        return searchWeb(query, "noLimit", true, 5);
    }
}