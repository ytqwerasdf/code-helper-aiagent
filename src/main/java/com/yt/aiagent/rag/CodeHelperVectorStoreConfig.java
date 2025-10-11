package com.yt.aiagent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Stream;

/**
 * 编程助手向量数据库配置（初始化基于内存的向量数据库 Bean）
 */
//@Configuration
public class CodeHelperVectorStoreConfig {

    @Resource
    private CodeHelperDocumentLoader codeHelperDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

//    @Bean
    VectorStore codeHelperVectorStore(EmbeddingModel dashscopeEmbeddingModel){
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        //加载文档
        List<Document> documents = codeHelperDocumentLoader.loadMarkdowns();
        List<Document> limitDocuments = documents.stream().limit(30).toList();
        //切分文档
//        List<Document> splitDocuments = myTokenTextSplitter.splitDocuments(limitDocuments);
        //利用ai补充关键词元信息
        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(limitDocuments);
        simpleVectorStore.add(enrichedDocuments);
        return simpleVectorStore;
    }

}
