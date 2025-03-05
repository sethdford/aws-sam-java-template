package com.enterprise.finance.personalization.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Mock service for generating embeddings.
 * This is a placeholder implementation until the Bedrock SDK is available.
 */
public class BedrockService implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(BedrockService.class);
    private final ObjectMapper objectMapper;
    private final Random random;
    private static final int EMBEDDING_DIMENSION = 1536;
    
    /**
     * Constructor that initializes the mock Bedrock service.
     */
    public BedrockService() {
        this.objectMapper = new ObjectMapper();
        this.random = new Random();
        logger.info("Initializing BedrockService");
    }
    
    /**
     * Generates a mock embedding vector for the given text.
     *
     * @param text The text to generate an embedding for
     * @return A mock embedding vector
     */
    public float[] generateEmbedding(String text) {
        logger.info("Generating mock embedding for text: {}", text.substring(0, Math.min(50, text.length())) + "...");
        
        // Generate a deterministic but random-looking embedding based on the text
        float[] embedding = new float[384]; // Standard embedding size
        
        // Use the text to seed the random generator for deterministic results
        random.setSeed(text.hashCode());
        
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = (random.nextFloat() * 2) - 1; // Values between -1 and 1
        }
        
        // Normalize the embedding
        float sum = 0;
        for (float value : embedding) {
            sum += value * value;
        }
        float magnitude = (float) Math.sqrt(sum);
        
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] /= magnitude;
        }
        
        return embedding;
    }
    
    /**
     * Generates mock embedding vectors for a list of texts.
     *
     * @param texts The list of texts to generate embeddings for
     * @return A list of mock embedding vectors
     */
    public List<float[]> generateEmbeddings(List<String> texts) {
        logger.info("Generating mock embeddings for {} texts", texts.size());
        
        List<float[]> embeddings = new ArrayList<>();
        for (String text : texts) {
            embeddings.add(generateEmbedding(text));
        }
        
        return embeddings;
    }
    
    /**
     * Calculates the cosine similarity between two embedding vectors.
     *
     * @param embeddings1 The first embedding vector
     * @param embeddings2 The second embedding vector
     * @return The cosine similarity value between 0 and 1
     */
    public double calculateCosineSimilarity(float[] embeddings1, float[] embeddings2) {
        if (embeddings1 == null || embeddings2 == null) {
            logger.warn("Cannot calculate similarity with null embeddings");
            return 0.0;
        }
        
        if (embeddings1.length != embeddings2.length) {
            logger.warn("Embedding dimensions do not match: {} vs {}", 
                    embeddings1.length, embeddings2.length);
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < embeddings1.length; i++) {
            dotProduct += embeddings1[i] * embeddings2[i];
            norm1 += embeddings1[i] * embeddings1[i];
            norm2 += embeddings2[i] * embeddings2[i];
        }
        
        if (norm1 <= 0.0 || norm2 <= 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    @Override
    public void close() {
        // No resources to close in the mock implementation
        logger.info("Closed mock BedrockService");
    }
} 