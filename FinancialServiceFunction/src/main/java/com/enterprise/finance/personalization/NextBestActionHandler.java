package com.enterprise.finance.personalization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.enterprise.finance.personalization.model.FinancialContext;
import com.enterprise.finance.personalization.model.UserBehavior;
import com.enterprise.finance.personalization.model.UserPreferences;
import com.enterprise.finance.personalization.service.BedrockService;
import com.enterprise.finance.personalization.service.MockOpenSearchService;
import com.enterprise.finance.personalization.service.NextBestActionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.tracing.Tracing;

/**
 * Lambda function handler for determining the next best actions for a user.
 */
public class NextBestActionHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(NextBestActionHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final NextBestActionService nextBestActionService;
    
    /**
     * Constructor that initializes the required services.
     */
    public NextBestActionHandler() {
        // Initialize services
        BedrockService bedrockService = new BedrockService();
        MockOpenSearchService openSearchService = new MockOpenSearchService();
        this.nextBestActionService = new NextBestActionService(bedrockService, openSearchService);
        
        // Configure ObjectMapper for proper date handling
        objectMapper.findAndRegisterModules(); // This registers JavaTimeModule for Instant serialization
        
        logger.info("NextBestActionHandler initialized");
    }
    
    /**
     * Handles the Lambda function request.
     *
     * @param input The API Gateway proxy request event
     * @param context The Lambda context
     * @return The API Gateway proxy response event
     */
    @Override
    @Tracing
    @Logging(logEvent = true)
    @Metrics(captureColdStart = true)
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        logger.info("Processing request: {}", input.getBody());
        
        try {
            // Parse the request body
            Map<String, Object> requestBody = objectMapper.readValue(input.getBody(), Map.class);
            
            // Extract user ID
            String userId = (String) requestBody.get("userId");
            if (userId == null || userId.isEmpty()) {
                return createErrorResponse(400, "Missing userId in request");
            }
            
            // Extract or create user preferences
            UserPreferences userPreferences;
            if (requestBody.containsKey("userPreferences")) {
                userPreferences = objectMapper.convertValue(requestBody.get("userPreferences"), UserPreferences.class);
            } else {
                userPreferences = new UserPreferences();
                userPreferences.setUserId(userId);
            }
            
            // Extract or create user behaviors
            List<UserBehavior> userBehaviors;
            if (requestBody.containsKey("userBehaviors")) {
                userBehaviors = objectMapper.convertValue(requestBody.get("userBehaviors"), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, UserBehavior.class));
            } else {
                userBehaviors = List.of();
            }
            
            // Extract or create financial context
            FinancialContext financialContext;
            if (requestBody.containsKey("financialContext")) {
                financialContext = objectMapper.convertValue(requestBody.get("financialContext"), FinancialContext.class);
            } else {
                financialContext = new FinancialContext();
                financialContext.setUserId(userId);
            }
            
            // Generate next best actions
            List<NextBestActionService.ActionRecommendation> recommendations = 
                nextBestActionService.determineNextBestActions(userPreferences, userBehaviors, financialContext);
            
            // Create response
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("userId", userId);
            responseBody.put("recommendations", recommendations);
            
            return createSuccessResponse(responseBody);
            
        } catch (Exception e) {
            logger.error("Error processing request", e);
            return createErrorResponse(500, "Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Creates a success response with the given body.
     *
     * @param body The response body
     * @return The API Gateway proxy response event
     */
    private APIGatewayProxyResponseEvent createSuccessResponse(Object body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);
        
        try {
            response.setBody(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            logger.error("Error serializing response", e);
            return createErrorResponse(500, "Error serializing response");
        }
        
        return response;
    }
    
    /**
     * Creates an error response with the given status code and message.
     *
     * @param statusCode The HTTP status code
     * @param message The error message
     * @return The API Gateway proxy response event
     */
    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);
        
        Map<String, String> errorBody = new HashMap<>();
        errorBody.put("error", message);
        
        try {
            response.setBody(objectMapper.writeValueAsString(errorBody));
        } catch (Exception e) {
            logger.error("Error serializing error response", e);
            response.setBody("{\"error\":\"Internal server error\"}");
        }
        
        return response;
    }
    
    /**
     * Creates CORS headers for the response.
     *
     * @return A map of CORS headers
     */
    private Map<String, String> createCorsHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        return headers;
    }
} 