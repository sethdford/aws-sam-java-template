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
import com.enterprise.finance.model.Transaction;
import com.enterprise.finance.model.TransactionStatus;
import com.enterprise.finance.repository.AccountRepository;
import com.enterprise.finance.repository.TransactionRepository;
import com.enterprise.finance.util.ApiGatewayResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.tracing.Tracing;

/**
 * Lambda function handler for processing financial transactions.
 * This handler processes API Gateway requests to create and process transactions.
 */
public class TransactionProcessor implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private static final Logger logger = LogManager.getLogger(TransactionProcessor.class);
    private final ObjectMapper objectMapper;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    
    public TransactionProcessor() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.transactionRepository = new TransactionRepository();
        this.accountRepository = new AccountRepository();
    }
    
    /**
     * Handles the Lambda function request to process a transaction.
     *
     * @param request The API Gateway request event
     * @param context The Lambda execution context
     * @return API Gateway response with transaction details or error information
     */
    @Override
    @Logging(logEvent = true)
    @Metrics(namespace = "FinancialServices", service = "TransactionService")
    @Tracing
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        logger.info("Processing transaction request");
        
        try {
            // Parse transaction from request body
            String requestBody = request.getBody();
            if (requestBody == null || requestBody.isEmpty()) {
                logger.error("Missing request body");
                return ApiGatewayResponseUtil.createErrorResponse(400, "Missing request body");
            }
            
            Transaction transaction = objectMapper.readValue(requestBody, Transaction.class);
            logger.info("Received transaction: {}", transaction);
            
            // Validate account exists
            Optional<Account> accountOptional = accountRepository.findById(transaction.getAccountId());
            if (!accountOptional.isPresent()) {
                logger.error("Account not found: {}", transaction.getAccountId());
                return ApiGatewayResponseUtil.createErrorResponse(404, "Account not found");
            }
            
            Account account = accountOptional.get();
            
            // Process transaction based on type
            boolean success = processTransaction(transaction, account);
            if (!success) {
                logger.error("Failed to process transaction");
                return ApiGatewayResponseUtil.createErrorResponse(400, "Failed to process transaction");
            }
            
            // Save transaction and update account
            transaction.setStatus(TransactionStatus.COMPLETED);
            Transaction savedTransaction = transactionRepository.save(transaction);
            accountRepository.save(account);
            
            // Return success response
            String transactionJson = objectMapper.writeValueAsString(savedTransaction);
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            
            return ApiGatewayResponseUtil.createSuccessResponse(201, transactionJson, headers);
            
        } catch (Exception e) {
            logger.error("Error processing transaction request", e);
            return ApiGatewayResponseUtil.createErrorResponse(500, "Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Processes a transaction by updating the account balance based on transaction type.
     *
     * @param transaction The transaction to process
     * @param account The account to update
     * @return true if processing was successful, false otherwise
     */
    @Tracing
    private boolean processTransaction(Transaction transaction, Account account) {
        try {
            String transactionType = transaction.getTransactionType().toUpperCase();
            double amount = transaction.getAmount();
            
            switch (transactionType) {
                case "DEPOSIT":
                    account.setBalance(account.getBalance() + amount);
                    break;
                case "WITHDRAWAL":
                    if (account.getBalance() < amount) {
                        logger.error("Insufficient funds for withdrawal. Balance: {}, Amount: {}", 
                                account.getBalance(), amount);
                        return false;
                    }
                    account.setBalance(account.getBalance() - amount);
                    break;
                default:
                    logger.error("Unsupported transaction type: {}", transactionType);
                    return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Error processing transaction", e);
            return false;
        }
    }
} 