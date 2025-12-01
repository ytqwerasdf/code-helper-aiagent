package com.yt.aiagent.es;


import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class ElasticSearchServiceImpl implements ElasticSearchService {

    @Resource
    private ElasticSearchRepository ElasticSearchRepository;


    @Override
    public void save(ElasticSearch ElasticSearch) {
        ElasticSearchRepository.save(ElasticSearch);
    }

    @Override
    public ElasticSearch findById(Integer id) {
        return ElasticSearchRepository.findById(id).orElse(new ElasticSearch());
    }

    @Override
    public void deleteById(Integer id) {
        ElasticSearchRepository.deleteById(id);
    }

    @Override
    public long count() {
        return ElasticSearchRepository.count();
    }

    @Override
    public boolean existsById(Integer id) {
        return ElasticSearchRepository.existsById(id);
    }

    @Override
    public List<ElasticSearch> findByKeyword(String keyword) {
        // 1. 声明变量（作用域覆盖整个方法）
        List<ElasticSearch> searchList = null;

        try {
            // 2. 执行查询（若 keyword 可能为 null，可先判空避免底层报错）
            if (keyword == null || keyword.trim().isEmpty()) {
                log.warn("查询关键词为空，返回空集合");
                return Collections.emptyList();
            }

            searchList = ElasticSearchRepository.findByKeyword(keyword);

            // 3. 若查询结果为 null，转为空集合（避免返回 null）
            if (CollectionUtils.isEmpty(searchList)) {
                log.info("关键词[{}]未查询到匹配结果", keyword);
                return Collections.emptyList();
            }

            // 4. 正常分支返回结果
            log.info("关键词[{}]查询成功，匹配到{}条记录", keyword, searchList.size());
            return searchList;

        } catch (Exception e) {
            // 5. 异常处理：打印堆栈+日志告警，返回空集合（不返回 null 避免上游 NPE）
            log.error("关键词[{}]查询失败", keyword, e);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<org.springframework.ai.document.Document> convertToDocument(List<ElasticSearch> elasticSearchList) {
        if (elasticSearchList == null || elasticSearchList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return elasticSearchList.stream()
                .map(es -> {
                    java.util.Map<String, Object> metadata = new java.util.HashMap<>();
                    metadata.put("id", es.getId());
                    metadata.put("title", es.getTitle());
                    metadata.put("category", es.getCategory());
                    metadata.put("filename", es.getFilename());
                    metadata.put("excerpt_keywords", es.getExcerpt_keywords());
                    // 也可以根据需要再加其他字段

                    return new org.springframework.ai.document.Document(
                            es.getContent() == null ? "" : es.getContent(),
                            metadata
                    );
                })
                .toList();
    }

}

