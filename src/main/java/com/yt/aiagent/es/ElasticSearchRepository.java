package com.yt.aiagent.es;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public interface ElasticSearchRepository extends ElasticsearchRepository<ElasticSearch, Integer> {

    /**
     * 使用单个关键词同时匹配 title、content 任意一个字段
     * @param keyword 查询关键字
     */
    @Query("""
            {
              "multi_match": {
                "query": "?0",
                "fields": ["title","content","excerpt_keywords"]
              }
            }
            """)
    List<ElasticSearch> findByKeyword(String keyword);

}

