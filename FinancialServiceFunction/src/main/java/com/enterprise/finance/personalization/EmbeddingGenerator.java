package com.enterprise.finance.personalization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.enterprise.finance.personalization.model.Product;
import com.enterprise.finance.personalization.repository.ProductRepository;
import com.enterprise.finance.personalization.service.BedrockService;
import com.enterprise.finance.personalization.service.OpenSearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.tracing.Tracing;

/**
 * Lambda function handler for generating embeddings for products and indexing them in OpenSearch.
 */
public class EmbeddingGenerator implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LogManager.getLogger(EmbeddingGenerator.class);
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;
    private final BedrockService bedrockService;
    private final OpenSearchService openSearchService;

    public EmbeddingGenerator() {
        this.objectMapper = new ObjectMapper();
        this.productRepository = new ProductRepository();
        this.bedrockService = new BedrockService();
        this.openSearchService = new OpenSearchService();
    }

    @Override
    @Tracing
    @Logging
    @Metrics
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        logger.info("Processing embedding generation request");
        
        try {
            // Parse request body
            Map<String, Object> requestBody = objectMapper.readValue(input.getBody(), Map.class);
            String action = (String) requestBody.getOrDefault("action", "generate");
            
            if ("generate".equals(action)) {
                // Generate embeddings for all products
                List<Product> products = productRepository.getAllProducts();
                logger.info("Retrieved {} products for embedding generation", products.size());
                
                int processedCount = 0;
                for (Product product : products) {
                    try {
                        // Generate embedding for product description
                        String textToEmbed = product.getName() + " " + product.getDescription() + " " + 
                                             product.getCategory();
                        
                        // Add additional product information if available
                        if (product.getFeatures() != null) {
                            textToEmbed += " " + product.getFeatures();
                        }
                        
                        float[] embedding = bedrockService.generateEmbedding(textToEmbed);
                        
                        // Store embedding in product attributes
                        product.addAttribute("embedding", embedding);
                        
                        // Save the updated product
                        productRepository.saveProduct(product);
                        
                        // Index product in OpenSearch
                        openSearchService.indexProduct(product);
                        processedCount++;
                        
                        logger.debug("Generated embedding for product: {}", product.getProductId());
                    } catch (Exception e) {
                        logger.error("Error processing product {}: {}", product.getProductId(), e.getMessage());
                    }
                }
                
                // Return success response
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("message", "Embedding generation completed");
                responseBody.put("processedCount", processedCount);
                responseBody.put("totalCount", products.size());
                
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(200)
                        .withBody(objectMapper.writeValueAsString(responseBody))
                        .withHeaders(Map.of("Content-Type", "application/json"));
                
            } else if ("search".equals(action)) {
                // Search for similar products based on text
                String searchText = (String) requestBody.get("searchText");
                int limit = requestBody.containsKey("limit") ? 
                        Integer.parseInt(requestBody.get("limit").toString()) : 10;
                
                if (searchText == null || searchText.isEmpty()) {
                    return createErrorResponse(400, "Search text is required");
                }
                
                // Generate embedding for search text
                float[] searchEmbedding = bedrockService.generateEmbedding(searchText);
                
                // Search for similar products in OpenSearch
                List<Product> results = openSearchService.searchSimilarProducts(searchEmbedding, limit);
                
                // Return search results
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("searchText", searchText);
                responseBody.put("results", results);
                responseBody.put("count", results.size());
                
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(200)
                        .withBody(objectMapper.writeValueAsString(responseBody))
                        .withHeaders(Map.of("Content-Type", "application/json"));
            } else {
                return createErrorResponse(400, "Invalid action. Supported actions: generate, search");
            }
            
        } catch (JsonProcessingException e) {
            logger.error("Error parsing request: {}", e.getMessage());
            return createErrorResponse(400, "Invalid request format: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing request: {}", e.getMessage());
            return createErrorResponse(500, "Internal server error: " + e.getMessage());
        }
    }
    
    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
        try {
            Map<String, String> errorResponse = Map.of("error", message);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withBody(objectMapper.writeValueAsString(errorResponse))
                    .withHeaders(Map.of("Content-Type", "application/json"));
        } catch (JsonProcessingException e) {
            logger.error("Error creating error response: {}", e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\":\"Internal server error\"}");
        }
    }
} 