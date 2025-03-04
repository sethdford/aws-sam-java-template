package com.enterprise.finance.repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.enterprise.finance.model.Transaction;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.lambda.powertools.tracing.Tracing;

/**
 * Repository class for Transaction entity operations with DynamoDB.
 */
public class TransactionRepository {
    
    private static final Logger logger = LogManager.getLogger(TransactionRepository.class);
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Transaction> transactionTable;
    private final String tableName;
    
    /**
     * Initializes the repository with DynamoDB client and table.
     */
    public TransactionRepository() {
        this.tableName = System.getenv("TRANSACTION_TABLE");
        
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
        
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        
        this.transactionTable = enhancedClient.table(tableName, TableSchema.fromBean(Transaction.class));
        logger.info("Initialized TransactionRepository with table: {}", tableName);
    }
    
    /**
     * Finds a transaction by its ID.
     *
     * @param transactionId The transaction ID to look up
     * @return Optional containing the transaction if found, empty otherwise
     */
    @Tracing
    public Optional<Transaction> findById(String transactionId) {
        logger.info("Finding transaction by ID: {}", transactionId);
        try {
            Key key = Key.builder().partitionValue(transactionId).build();
            Transaction transaction = transactionTable.getItem(key);
            return Optional.ofNullable(transaction);
        } catch (Exception e) {
            logger.error("Error finding transaction by ID: {}", transactionId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Finds all transactions for an account.
     *
     * @param accountId The account ID to look up transactions for
     * @return List of transactions for the account
     */
    @Tracing
    public List<Transaction> findByAccountId(String accountId) {
        logger.info("Finding transactions for account ID: {}", accountId);
        try {
            QueryConditional queryConditional = QueryConditional
                    .keyEqualTo(Key.builder().partitionValue(accountId).build());
            
            List<Transaction> transactions = new ArrayList<>();
            transactionTable.index("AccountTransactions")
                    .query(queryConditional)
                    .forEach(page -> page.items().forEach(transactions::add));
            
            return transactions;
        } catch (Exception e) {
            logger.error("Error finding transactions for account ID: {}", accountId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Finds transactions for an account within a date range.
     *
     * @param accountId The account ID to look up transactions for
     * @param startDate The start date for the range
     * @param endDate The end date for the range
     * @return List of transactions for the account within the date range
     */
    @Tracing
    public List<Transaction> findByAccountIdAndDateRange(String accountId, Instant startDate, Instant endDate) {
        logger.info("Finding transactions for account ID: {} between {} and {}", accountId, startDate, endDate);
        try {
            String startDateStr = startDate.toString();
            String endDateStr = endDate.toString();
            
            QueryConditional queryConditional = QueryConditional
                    .sortBetween(
                            Key.builder().partitionValue(accountId).sortValue(startDateStr).build(),
                            Key.builder().partitionValue(accountId).sortValue(endDateStr).build());
            
            List<Transaction> transactions = new ArrayList<>();
            transactionTable.index("AccountTransactions")
                    .query(queryConditional)
                    .forEach(page -> page.items().forEach(transactions::add));
            
            return transactions;
        } catch (Exception e) {
            logger.error("Error finding transactions for account ID: {} in date range", accountId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Saves a transaction to the database.
     *
     * @param transaction The transaction to save
     * @return The saved transaction
     */
    @Tracing
    public Transaction save(Transaction transaction) {
        logger.info("Saving transaction: {}", transaction);
        try {
            transactionTable.putItem(transaction);
            return transaction;
        } catch (Exception e) {
            logger.error("Error saving transaction: {}", transaction, e);
            throw new RuntimeException("Failed to save transaction", e);
        }
    }
} 