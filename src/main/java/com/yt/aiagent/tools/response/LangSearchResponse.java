package com.yt.aiagent.tools.response;

import lombok.Data;

/**
 * LangSearch API 响应实体类
 */
@Data
public class LangSearchResponse {
    private String id;
    private String name;
    private String url;
    private String displayUrl;
    private String snippet;
    private String summary;
    private String datePublished;
    private String dateLastCrawled;
}