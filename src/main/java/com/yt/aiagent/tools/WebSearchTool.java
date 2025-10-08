package com.yt.aiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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

    @Value("${search-api.api-key}")
    private  String apiKey;

    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(
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
            // 只有当存在数据时才截取，否则返回空列表
            List<Object> objects = endIndex > 0 ? organicResults.subList(0, endIndex) : new ArrayList<>();
            //拼接搜索结果为字符串
            String result = objects.stream().map(obj -> {
                JSONObject tmpJSONObject = (JSONObject) obj;
                return tmpJSONObject.toString();
            }).collect(Collectors.joining(","));
            return result;
        }catch (Exception e){
            return "Error searching Baidu: "+e.getMessage();
        }
    }
}
