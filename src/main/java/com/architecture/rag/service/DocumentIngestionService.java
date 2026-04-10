package com.architecture.rag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DocumentIngestionService {

    private static final Logger logger = LogManager.getLogger(DocumentIngestionService.class);
    private final EmbeddingStoreIngestor ingestor;

    public DocumentIngestionService(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        // Splitting large API specs into chunks of 500 characters with a 50-character overlap
        // Overlap prevents cutting crucial JSON objects or schema definitions in half
        this.ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 50))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    /**
     * Ingests raw API documentation into the Vector Database.
     */
    public void ingestApiDocumentation(String rawApiDoc) {
        logger.info("Starting ingestion of raw API documentation into Vector Store...");
        Document document = Document.from(rawApiDoc);
        ingestor.ingest(document);
        logger.info("Successfully chunked and embedded documentation.");
    }
}
