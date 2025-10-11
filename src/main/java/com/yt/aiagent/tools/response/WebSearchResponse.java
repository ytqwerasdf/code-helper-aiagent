package com.yt.aiagent.tools.response;

import lombok.Builder;
import lombok.Data;

/**
 * 网页搜索工具返回对象
 *
 */
@Data
@Builder
public class WebSearchResponse {
    /**
     * 标题
     */
    private String title;

    /**
     * 链接
     */
    private String link;

    /**
     *内容片段
     */
    private String snippet;

}
