package com.enterprise.finance.personalization;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.enterprise.finance.personalization.model.UserBehavior;
import com.enterprise.finance.personalization.model.UserPreference;
import com.enterprise.finance.personalization.repository.UserBehaviorRepository;
import com.enterprise.finance.personalization.repository.UserPreferenceRepository;
import com.enterprise.finance.util.ApiGatewayResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.tracing.Tracing;

/**
 * Lambda function handler for tracking user behavior events.
 * This handler processes API Gateway requests to record user behavior for personalization.
 */
public class UserBehaviorTracker implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private static final Logger logger = LogManager.getLogger(UserBehaviorTracker.class);
    private final ObjectMapper objectMapper;
    private final UserBehaviorRepository userBehaviorRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    
    public UserBehaviorTracker() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.userBehaviorRepository = new UserBehaviorRepository();
        this.userPreferenceRepository = new UserPreferenceRepository();
    }
    
    /**
     * Handles the Lambda function request to track user behavior.
     *
     * @param request The API Gateway request event
     * @param context The Lambda execution context
     * @return API Gateway response with success or error information
     */
    @Override
    @Logging(logEvent = true)
    @Metrics(namespace = "FinancialServices", service = "PersonalizationService")
    @Tracing
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        logger.info("Processing user behavior tracking request");
        
        try {
            // Parse behavior event from request body
            String requestBody = request.getBody();
            if (requestBody == null || requestBody.isEmpty()) {
                logger.error("Missing request body");
                return ApiGatewayResponseUtil.createErrorResponse(400, "Missing request body");
            }
            
            UserBehavior userBehavior = objectMapper.readValue(requestBody, UserBehavior.class);
            logger.info("Received user behavior event: {}", userBehavior);
            
            // Save the behavior event
            UserBehavior savedBehavior = userBehaviorRepository.save(userBehavior);
            
            // Update user preferences based on behavior
            updateUserPreferences(userBehavior);
            
            // Return success response
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            
            return ApiGatewayResponseUtil.createSuccessResponse(201, 
                    objectMapper.writeValueAsString(savedBehavior), headers);
            
        } catch (Exception e) {
            logger.error("Error processing user behavior tracking request", e);
            return ApiGatewayResponseUtil.createErrorResponse(500, "Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Updates user preferences based on behavior data.
     *
     * @param userBehavior The user behavior event
     */
    @Tracing
    private void updateUserPreferences(UserBehavior userBehavior) {
        try {
            String userId = userBehavior.getUserId();
            String actionType = userBehavior.getActionType();
            String resourceId = userBehavior.getResourceId();
            String resourceType = userBehavior.getResourceType();
            
            if (userId == null || actionType == null) {
                logger.warn("Cannot update preferences: Missing required fields in behavior event");
                return;
            }
            
            // Get current user preferences or create new if not exists
            UserPreference userPreference;
            Optional<UserPreference> optionalPreference = userPreferenceRepository.findByUserId(userId);
            if (optionalPreference.isPresent()) {
                userPreference = optionalPreference.get();
            } else {
                userPreference = new UserPreference();
                userPreference.setUserId(userId);
            }
            
            // Update category preferences based on behavior
            if (resourceType != null && "VIEW".equals(actionType)) {
                // Increment view count for this category (weight: 1)
                userPreference.updateCategoryPreference(resourceType, 1);
            } else if (resourceType != null && "CLICK".equals(actionType)) {
                // Increment click count for this category (weight: 2)
                userPreference.updateCategoryPreference(resourceType, 2);
            } else if (resourceType != null && "PURCHASE".equals(actionType)) {
                // Increment purchase count for this category (weight: 5)
                userPreference.updateCategoryPreference(resourceType, 5);
            }
            
            // Save updated preferences
            userPreferenceRepository.save(userPreference);
            logger.info("Updated user preferences for user ID: {}", userId);
            
        } catch (Exception e) {
            logger.error("Error updating user preferences: {}", e.getMessage());
        }
    }
} 