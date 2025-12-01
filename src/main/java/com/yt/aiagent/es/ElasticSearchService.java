package com.yt.aiagent.es;


import org.springframework.ai.document.Document;

import java.util.List;

public interface ElasticSearchService {

    //保存和修改
    void save(ElasticSearch article);
    //查询id
    ElasticSearch findById(Integer id);
    //删除指定ID数据
    void   deleteById(Integer id);

    long count();
    
    boolean existsById(Integer id);

    List<ElasticSearch> findByKeyword(String keyword);

    List<Document> convertToDocument(List<ElasticSearch> elasticSearchList);

}

