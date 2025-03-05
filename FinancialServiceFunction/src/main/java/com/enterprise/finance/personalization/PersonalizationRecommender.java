package com.enterprise.finance.personalization;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.enterprise.finance.personalization.model.UserPreference;
import com.enterprise.finance.personalization.repository.UserPreferenceRepository;
import com.enterprise.finance.personalization.service.OpenSearchService;
import com.enterprise.finance.util.ApiGatewayResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.tracing.Tracing;

/**
 * Lambda function handler for generating personalized recommendations.
 * This handler processes API Gateway requests to provide personalized product recommendations.
 */
public class PersonalizationRecommender implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private static final Logger logger = LogManager.getLogger(PersonalizationRecommender.class);
    private final ObjectMapper objectMapper;
    private final UserPreferenceRepository userPreferenceRepository;
    private final OpenSearchService openSearchService;
    
    public PersonalizationRecommender() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.userPreferenceRepository = new UserPreferenceRepository();
        this.openSearchService = new OpenSearchService();
    }
    
    /**
     * Handles the Lambda function request to generate personalized recommendations.
     *
     * @param request The API Gateway request event
     * @param context The Lambda execution context
     * @return API Gateway response with recommendations or error information
     */
    @Override
    @Logging(logEvent = true)
    @Metrics(namespace = "FinancialServices", service = "PersonalizationService")
    @Tracing
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        logger.info("Processing personalization recommendation request");
        
        try {
            // Extract customer ID from path parameters
            Map<String, String> pathParameters = request.getPathParameters();
            if (pathParameters == null || !pathParameters.containsKey("customerId")) {
                logger.error("Missing required path parameter: customerId");
                return ApiGatewayResponseUtil.createErrorResponse(400, "Missing required path parameter: customerId");
            }
            
            String customerId = pathParameters.get("customerId");
            logger.info("Generating recommendations for customer ID: {}", customerId);
            
            // Get query parameters
            Map<String, String> queryParameters = request.getQueryStringParameters();
            int limit = 10; // Default limit
            String category = null;
            
            if (queryParameters != null) {
                if (queryParameters.containsKey("limit")) {
                    try {
                        limit = Integer.parseInt(queryParameters.get("limit"));
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid limit parameter, using default: {}", limit);
                    }
                }
                
                if (queryParameters.containsKey("category")) {
                    category = queryParameters.get("category");
                }
            }
            
            // Get user preferences
            Optional<UserPreference> userPreferenceOptional = userPreferenceRepository.findByUserId(customerId);
            
            // Generate recommendations
            List<String> recommendedProductIds;
            
            if (userPreferenceOptional.isPresent()) {
                UserPreference userPreference = userPreferenceOptional.get();
                
                // If category is specified, filter by that category
                if (category != null && !category.isEmpty()) {
                    Map<String, Integer> categoryPreference = new HashMap<>();
                    categoryPreference.put(category, 10); // High weight for the requested category
                    recommendedProductIds = openSearchService.searchPersonalizedProducts(customerId, categoryPreference, limit);
                } else {
                    // Use all category preferences
                    recommendedProductIds = openSearchService.searchPersonalizedProducts(
                            customerId, userPreference.getCategoryPreferences(), limit);
                }
            } else {
                // No preferences found, return default recommendations
                logger.info("No preferences found for customer ID: {}, returning default recommendations", customerId);
                Map<String, Integer> defaultPreferences = new HashMap<>();
                
                // If category is specified, use that
                if (category != null && !category.isEmpty()) {
                    defaultPreferences.put(category, 5);
                } else {
                    // Default categories
                    defaultPreferences.put("INVESTMENT", 5);
                    defaultPreferences.put("SAVINGS", 5);
                    defaultPreferences.put("RETIREMENT", 5);
                }
                
                recommendedProductIds = openSearchService.searchPersonalizedProducts(customerId, defaultPreferences, limit);
            }
            
            // Create response
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("customerId", customerId);
            responseBody.put("recommendations", recommendedProductIds);
            responseBody.put("count", recommendedProductIds.size());
            
            // Return success response
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            
            return ApiGatewayResponseUtil.createSuccessResponse(200, 
                    objectMapper.writeValueAsString(responseBody), headers);
            
        } catch (Exception e) {
            logger.error("Error processing personalization recommendation request", e);
            return ApiGatewayResponseUtil.createErrorResponse(500, "Internal server error: " + e.getMessage());
        } finally {
            // Close OpenSearch client
            try {
                openSearchService.close();
            } catch (IOException e) {
                logger.error("Error closing OpenSearch client", e);
            }
        }
    }
} 