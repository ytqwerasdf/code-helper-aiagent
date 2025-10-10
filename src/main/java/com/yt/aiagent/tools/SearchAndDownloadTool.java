package com.yt.aiagent.tools;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.List;

public class SearchAndDownloadTool {

    @Resource
    ResourceDownloadTool resourceDownloadTool;

    @Resource
    ImageSearchTool imageSearchTool;

    @Tool(description = "Search and download images for a specific topic")
    public String searchAndDownloadImages(
            @ToolParam(description = "Search query for images") String query,
            @ToolParam(description = "Number of images to download") int count) {

        try {
            // 使用现有的ImageSearchTool搜索图片
            String imageUrls = imageSearchTool.searchImage(query);

            if (StrUtil.isBlank(imageUrls)) {
                return "No images found for query: " + query;
            }

            String[] urls = imageUrls.split(",");
            List<String> downloadedImages = new ArrayList<>();

            // 下载指定数量的图片
            int downloadCount = Math.min(count, urls.length);
            for (int i = 0; i < downloadCount; i++) {
                String url = urls[i].trim();
                String fileName = "image_" + query.replace(" ", "_") + "_" + i + ".jpg";
                String result = resourceDownloadTool.downloadResource(url, fileName);
                if (result.contains("successfully")) {
                    downloadedImages.add(fileName);
                }
            }

            return "Downloaded " + downloadedImages.size() + " images: " +
                    String.join(", ", downloadedImages);
        } catch (Exception e) {
            return "Error searching and downloading images: " + e.getMessage();
        }
    }
}
