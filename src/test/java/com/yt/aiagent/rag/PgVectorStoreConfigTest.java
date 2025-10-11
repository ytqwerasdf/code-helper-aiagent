package com.yt.aiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class PgVectorStoreConfigTest {

    @Resource
    private VectorStore pgVectorStore;

    @Resource
    private CodeHelperDocumentLoader codeHelperDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Test
    void pgVectorStore() {
//        List<Document> documents = List.of(
//                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
//                new Document("The World is Big and Salvation Lurks Around the Corner"),
//                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));
        List<Document> documents = codeHelperDocumentLoader.loadMarkdowns();
        List<Document> limitDocuments = documents.stream().limit(30).toList();

        //切分文档
        List<Document> splitDocuments = myTokenTextSplitter.splitDocuments(limitDocuments);
        //利用ai补充关键词元信息
        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(splitDocuments);

// Add the documents to PGVector
        pgVectorStore.add(enrichedDocuments);

// Retrieve documents similar to a query
        List<Document> results = this.pgVectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
        log.info(results.toString());
        Assertions.assertNotNull(results);
    }
}