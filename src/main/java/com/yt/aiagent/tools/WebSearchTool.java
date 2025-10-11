package com.yt.aiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yt.aiagent.tools.response.WebSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网页搜索工具
 */
@Component
public class WebSearchTool {
    //SearchAPI 的搜索接口地址
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";
    private static final Logger log = LoggerFactory.getLogger(WebSearchTool.class);

    @Value("${search-api.api-key}")
    private  String apiKey;

    @Tool(description = "Search for information from Baidu Search Engine(Backup search tool)")
    public String spareSearchWeb(
            @ToolParam(description = "Search query keyword") String query){
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("q",query);
        paramMap.put("api_key",apiKey);
        paramMap.put("engine","baidu");
        try {
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);
            //取出返回结果的前5条
            JSONObject jsonObject = JSONUtil.parseObj(response);
            //取出 organic_results 部分
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            // 计算实际能取到的最大索引（取最小值，避免越界）
            int endIndex = Math.min(organicResults.size(), 5);
            List<WebSearchResponse> results = new ArrayList<>();
            // 只有当存在数据时才截取，否则返回空列表
            if (endIndex > 0) {
                // 遍历前N条结果并转换为WebSearchResponse对象
                for (int i = 0; i < endIndex; i++) {
                    JSONObject item = organicResults.getJSONObject(i);
                    // 转换为实体类
                    WebSearchResponse searchResponse = JSONUtil.toBean(item, WebSearchResponse.class);
                    results.add(searchResponse);
                }
            }

            //拼接搜索结果为字符串
            String result = results.stream()
                    .map(webRes -> "Title：" + webRes.getTitle() + "\nLink："+webRes.getLink()+"\nSnippet: " + webRes.getSnippet() + "\n\n")
                    .collect(Collectors.joining());
            log.info(result);
            return result;
        }catch (Exception e){
            return "Error searching Baidu: "+e.getMessage();
        }
    }
}
