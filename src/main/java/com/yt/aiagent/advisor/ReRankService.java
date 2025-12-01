package com.yt.aiagent.advisor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * LangSearch Semantic Rerank API 调用服务。
 * 文档参考: https://docs.langsearch.com/api/semantic-rerank-api
 */
@Slf4j
@Component
public class ReRankService {

    private static final String DEFAULT_MODEL = "langsearch-reranker-v1";
    private static final String DEFAULT_ENDPOINT = "https://api.langsearch.com/v1/rerank";

    private final WebClient webClient;

    private final String apiKey;

    public ReRankService(WebClient.Builder webClientBuilder,
                         @Value("${langsearch.api-key:}") String apiKey) {
        this.webClient = webClientBuilder.baseUrl(DEFAULT_ENDPOINT).build();
        this.apiKey = apiKey;
    }

    /**
     * 同步重排调用（阻塞获取结果）。
     */
    public List<RerankResult> reranks(String query, List<String> documents, Integer topN, boolean returnDocuments) {
        return rerankAsync(query, documents, topN, returnDocuments)
                .onErrorResume(e -> {
                    log.error("LangSearch rerank 调用失败: {}", e.getMessage(), e);
                    return Mono.just(Collections.emptyList());
                })
                .block();
    }

    /**
     * 同步重排调用（阻塞获取结果）。
     */
    public List<Document> rerank(String query, List<Document> documents, Integer topN, boolean returnDocuments) {
        List<String> strDocuments = new ArrayList<>();
        List<Document> results = new ArrayList<>();
        for (Document document : documents) {
            strDocuments.add(document.getText());
        }

        // 若条目数超过 50，则按块合并为 ≤50 段
        strDocuments = mergeToAtMost(strDocuments, 50, "\n---\n");

        List<RerankResult> rerankResults = rerankAsync(query, strDocuments, topN, returnDocuments)
                .onErrorResume(e -> {
                    log.error("LangSearch rerank 调用失败: {}", e.getMessage(), e);
                    return Mono.just(Collections.emptyList());
                })
                .block();
        for (RerankResult rerankResult : rerankResults) {
            results.add(new Document(rerankResult.getDocument().getText()));
        }
        return results;
    }

    /**
     * 异步重排调用。
     */
    public Mono<List<RerankResult>> rerankAsync(String query, List<String> documents, Integer topN, boolean returnDocuments) {
        validate(query, documents, topN);

        Assert.hasText(apiKey, "缺少 langsearch.api-key 配置");

        RerankRequest request = new RerankRequest();
        request.setModel(DEFAULT_MODEL);
        request.setQuery(query);
        request.setDocuments(documents);
        if (topN != null) {
            request.setTop_n(topN);
        }
        request.setReturn_documents(returnDocuments);

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RerankResponse.class)
                .map(resp -> {
                    if (resp == null || resp.getCode() == null) {
                        throw new IllegalStateException("重排返回为空");
                    }
                    if (!Objects.equals(resp.getCode(), 200)) {
                        throw new IllegalStateException("重排失败, code=" + resp.getCode() + ", msg=" + resp.getMsg());
                    }
                    return resp.getResults();
                });
    }

    private static List<String> mergeToAtMost(List<String> items, int maxChunks, String separator) {
        // 基本校验
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        if (maxChunks <= 0) {
            throw new IllegalArgumentException("maxChunks must be > 0");
        }
        if (items.size() <= maxChunks) {
            return items;
        }
        if (separator == null) {
            separator = "";
        }

        int n = items.size();
        // 计算每个合并块包含的元素数，向上取整
        int chunkSize = (n + maxChunks - 1) / maxChunks;

        List<String> merged = new ArrayList<>(Math.min(maxChunks, n));
        for (int i = 0; i < n; i++, i += (chunkSize - 1)) {
            int end = Math.min(i + chunkSize, n);
            merged.add(String.join(separator, items.subList(i, end)));
        }
        return merged;
    }

    private static void validate(String query, List<String> documents, Integer topN) {
        Assert.hasText(query, "query 不能为空");
        Assert.notEmpty(documents, "documents 不能为空");
        Assert.isTrue(documents.size() <= 50, "documents 数量不能超过 50");
        if (topN != null) {
            Assert.isTrue(topN > 0 && topN <= documents.size(), "topN 必须在 1..documents.size() 之间");
        }
    }

    // ===== DTOs =====

    @Data
    public static class RerankRequest {
        private String model;                 // 必填: langsearch-reranker-v1
        private String query;                 // 必填
        private List<String> documents;       // 必填（最多50）
        private Integer top_n;                // 选填
        private Boolean return_documents;     // 选填
    }

    @Data
    public static class RerankResponse {
        private Integer code;
        private String log_id;
        private String msg;
        private String model;
        private List<RerankResult> results;
    }

    @Data
    public static class RerankResult {
        private Integer index;  //index of origin document list
        private RerankDocument document; // 当 return_documents=true 时返回
        private Double relevance_score;   // 0..1 越大越相关
    }

    @Data
    public static class RerankDocument {
        private String text;
    }
}

