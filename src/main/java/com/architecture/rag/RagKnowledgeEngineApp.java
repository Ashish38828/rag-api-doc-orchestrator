package com.architecture.rag;

import com.architecture.rag.service.DocumentIngestionService;
import com.architecture.rag.service.RetrievalAgentService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RagKnowledgeEngineApp {

    private static final Logger logger = LogManager.getLogger(RagKnowledgeEngineApp.class);

    public static void main(String[] args) {
        logger.info("Initializing Enterprise RAG API Knowledge Engine...");

        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("OPENAI_API_KEY");
        String chromaUrl = dotenv.get("CHROMA_URL", "http://localhost:8000");

        if (apiKey == null || apiKey.equals("your_openai_api_key_here")) {
            logger.fatal("Missing OPENAI_API_KEY. Shutting down system.");
            System.exit(1);
        }

        // Initialize LangChain4j Components
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(dotenv.get("EMBEDDING_MODEL", "text-embedding-3-small"))
                .build();

        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(dotenv.get("MODEL_NAME", "gpt-4o"))
                .temperature(0.0) // 0.0 Temperature is MANDATORY for strict RAG data extraction
                .build();

        // Connect to local ChromaDB container
        EmbeddingStore<dev.langchain4j.data.segment.TextSegment> chromaStore = ChromaEmbeddingStore.builder()
                .baseUrl(chromaUrl)
                .collectionName("api-specs")
                .build();

        DocumentIngestionService ingestionService = new DocumentIngestionService(embeddingModel, chromaStore);
        RetrievalAgentService retrievalService = new RetrievalAgentService(chatModel, embeddingModel, chromaStore);

        // Mock Scenario: Ingesting an internal swagger doc
        String mockSwaggerDoc = """
            ENDPOINT: POST /api/v1/logistics/transfer
            DESCRIPTION: Initiates a stock transfer between warehouses.
            REQUIRED PAYLOAD:
            {
              "sourceWarehouseId": "string (UUID)",
              "destinationWarehouseId": "string (UUID)",
              "sku": "string (Alphanumeric)",
              "quantity": "integer (must be > 0)",
              "priorityTransfer": "boolean"
            }
            """;

        try {
            // Phase 1: Ingestion
            ingestionService.ingestApiDocumentation(mockSwaggerDoc);

            // Phase 2: Retrieval & Generation
            String userQuery = "Generate a valid JSON payload for a high-priority stock transfer of 50 units.";
            String generatedPayload = retrievalService.generateTestPayload(userQuery);

            logger.info("\n================ GENERATED API PAYLOAD ================\n{}\n=======================================================", generatedPayload);

        } catch (Exception e) {
            logger.error("RAG Pipeline execution failed. Is ChromaDB running via Docker?", e);
        }
    }
}
