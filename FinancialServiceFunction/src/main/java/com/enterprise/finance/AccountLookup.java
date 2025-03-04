package com.enterprise.finance;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.enterprise.finance.model.Account;
import com.enterprise.finance.repository.AccountRepository;
import com.enterprise.finance.util.ApiGatewayResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.tracing.Tracing;

/**
 * Lambda function handler for looking up account information.
 * This handler processes API Gateway requests to retrieve account details.
 */
public class AccountLookup implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private static final Logger logger = LogManager.getLogger(AccountLookup.class);
    private final ObjectMapper objectMapper;
    private final AccountRepository accountRepository;
    
    public AccountLookup() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.accountRepository = new AccountRepository();
    }
    
    /**
     * Handles the Lambda function request to look up an account.
     *
     * @param request The API Gateway request event
     * @param context The Lambda execution context
     * @return API Gateway response with account details or error information
     */
    @Override
    @Logging(logEvent = true)
    @Metrics(namespace = "FinancialServices", service = "AccountService")
    @Tracing
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        logger.info("Processing account lookup request");
        
        try {
            // Extract account ID from path parameters
            Map<String, String> pathParameters = request.getPathParameters();
            if (pathParameters == null || !pathParameters.containsKey("accountId")) {
                logger.error("Missing required path parameter: accountId");
                return ApiGatewayResponseUtil.createErrorResponse(400, "Missing required path parameter: accountId");
            }
            
            String accountId = pathParameters.get("accountId");
            logger.info("Looking up account with ID: {}", accountId);
            
            // Retrieve account from repository
            Optional<Account> accountOptional = accountRepository.findById(accountId);
            
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();
                String accountJson = objectMapper.writeValueAsString(account);
                
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Custom-Header", "Financial Services API");
                
                return ApiGatewayResponseUtil.createSuccessResponse(200, accountJson, headers);
            } else {
                logger.warn("Account not found with ID: {}", accountId);
                return ApiGatewayResponseUtil.createErrorResponse(404, "Account not found");
            }
            
        } catch (Exception e) {
            logger.error("Error processing account lookup request", e);
            return ApiGatewayResponseUtil.createErrorResponse(500, "Internal server error: " + e.getMessage());
        }
    }
} 