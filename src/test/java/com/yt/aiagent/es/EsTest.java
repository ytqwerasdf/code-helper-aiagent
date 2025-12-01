package com.yt.aiagent.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class EsTest {

    @Resource
    private ElasticSearchService elasticSearchService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Test
    public void searchTest() throws JsonProcessingException {
        String sql = "SELECT metadata, content FROM vector_store";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        int num = 1;

        for (Map<String, Object> row : rows) {
            Object metadataObj = row.get("metadata");
            String metadataJson;
            if (metadataObj instanceof String) {
                metadataJson = (String) metadataObj;
            } else if (metadataObj instanceof org.postgresql.util.PGobject pgObject) {
                metadataJson = pgObject.getValue();
            } else {
                throw new IllegalStateException("Unsupported metadata type: " + metadataObj);
            }

            String content = (String) row.get("content");

            Map<String, Object> metadata = objectMapper.readValue(metadataJson, new TypeReference<>() {});
            ElasticSearch doc = new ElasticSearch();
            doc.setId(num++);
            doc.setTitle((String) metadata.getOrDefault("title", ""));
            doc.setCategory((String) metadata.getOrDefault("category", ""));
            doc.setFilename((String) metadata.getOrDefault("filename", ""));
            doc.setExcerpt_keywords((String) metadata.getOrDefault("excerpt_keywords", ""));
            doc.setContent(content);

            elasticSearchService.save(doc);
        }
    }

    /**创建索引和映射*/
    @Test
    public void createIndex(){

//        elasticsearchTemplate.createIndex(ElasticSearch.class);
//        elasticsearchTemplate.putMapping(ElasticSearch.class);
    }

    /**添加文档或者修改文档(以id为准)*/
    @Test
    public void saveElasticSearch(){
        ElasticSearch elasticSearch = new ElasticSearch();
        elasticSearch.setId(1);
        elasticSearch.setTitle("SpringData ElasticSearch");
        elasticSearch.setContent("Spring Data ElasticSearch 基于 spring data API 简化 elasticSearch操作，将原始操作elasticSearch的客户端API 进行封装 \n" +
                "    Spring Data为Elasticsearch Elasticsearch项目提供集成搜索引擎");
        elasticSearchService.save(elasticSearch);
    }
    @Test
    public void findById(){
        ElasticSearch byId = elasticSearchService.findById(1);
        System.out.println(byId);
    }
    @Test
    public void deleteById(){
        elasticSearchService.deleteById(100);

    }
    @Test
    public void count(){
        long count = elasticSearchService.count();
        System.out.println(count);
    }
    @Test
    public void existsById(){
        boolean b = elasticSearchService.existsById(102);

        System.out.println(b);
    }
    @Test
    public void findByTitleOrContent(){
        List<ElasticSearch> list = elasticSearchService.findByKeyword("我要学习Java程序语言");
        list.forEach(System.out::println);
    }
}

