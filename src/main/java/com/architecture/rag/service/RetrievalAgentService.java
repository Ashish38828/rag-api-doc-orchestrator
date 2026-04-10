package com.architecture.rag.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class RetrievalAgentService {

    private static final Logger logger = LogManager.getLogger(RetrievalAgentService.class);
    private final ChatLanguageModel chatModel;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public RetrievalAgentService(ChatLanguageModel chatModel, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    /**
     * Queries the Vector DB for context, then prompts the LLM to generate an API test payload.
     */
    public String generateTestPayload(String userQuery) {
        logger.info("Executing Vector Similarity Search for query: {}", userQuery);
        
        // 1. Convert user query into a vector and search ChromaDB for the top 2 closest matches
        List<EmbeddingMatch<TextSegment>> relevantMatches = embeddingStore.findRelevant(embeddingModel.embed(userQuery).content(), 2);
        
        StringBuilder retrievedContext = new StringBuilder();
        for (EmbeddingMatch<TextSegment> match : relevantMatches) {
            retrievedContext.append(match.embedded().text()).append("\n");
        }

        logger.debug("Retrieved Context from Vector Store: {}", retrievedContext.toString());

        // 2. Construct the strict RAG Prompt
        String prompt = String.format("""
            You are an API Test Architect. Your task is to generate a JSON test payload for the user's request.
            
            CRITICAL INSTRUCTION: You must ONLY use the provided "System Context" to build the JSON. 
            Do NOT hallucinate fields or use external knowledge. If the context does not contain enough info, reply with "INSUFFICIENT CONTEXT".
            
            System Context:
            %s
            
            User Request: %s
            """, retrievedContext.toString(), userQuery);

        logger.info("Generating payload via LLM...");
        return chatModel.generate(prompt);
    }
}
