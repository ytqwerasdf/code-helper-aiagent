package com.yt.aiagent.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 图片搜索工具
 */
@Component
public class ImageSearchTool {

    @Value("${pexels.api-key}")
    private String API_KEY;

    private static final String API_URL = "https://api.pexels.com/v1/search";

    @Tool(description = "search image from web")
    public String searchImage(@ToolParam(description = "search query keyword") String query){
        try {
            List<String> images = searchMediumImages(query);
            if(images.isEmpty())return null;
            return String.join(",",images);
        } catch (Exception e) {
            return "Error search image: "+e.getMessage();
        }
    }


    /**
     * 搜索中等尺寸的图片列表
     * @param query
     * @return
     */
    private List<String> searchMediumImages(String query){
        try {
            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", API_KEY);

            // 设置请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("query", query);
            params.put("per_page", 10); // 限制返回数量

            // 发送GET请求
            String response = HttpUtil.createGet(API_URL)
                    .addHeaders(headers)
                    .form(params)
                    .execute()
                    .body();

            // 添加调试日志
            System.out.println("API Response: " + response);

            // 解析响应的JSON
            JSONObject jsonResponse = JSONUtil.parseObj(response);

            // 检查是否有错误
            if (jsonResponse.containsKey("error")) {
                throw new RuntimeException("API Error: " + jsonResponse.getStr("error"));
            }

            // 检查是否有photos字段
            if (!jsonResponse.containsKey("photos")) {
                throw new RuntimeException("No photos field in response: " + response);
            }

            JSONArray photos = jsonResponse.getJSONArray("photos");
            if (photos == null || photos.isEmpty()) {
                return new ArrayList<>();
            }

            return photos.stream()
                    .map(photoObj -> (JSONObject) photoObj)
                    .map(photoObj -> photoObj.getJSONObject("src"))
                    .filter(Objects::nonNull)
                    .map(photo -> photo.getStr("medium"))
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error in searchMediumImages: " + e.getMessage());
            throw new RuntimeException("Failed to search images: " + e.getMessage(), e);
        }
    }
}
