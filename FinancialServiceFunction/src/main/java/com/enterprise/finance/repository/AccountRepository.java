package com.enterprise.finance.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.enterprise.finance.model.Account;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.lambda.powertools.tracing.Tracing;

/**
 * Repository class for Account entity operations with DynamoDB.
 */
public class AccountRepository {
    
    private static final Logger logger = LogManager.getLogger(AccountRepository.class);
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Account> accountTable;
    private final String tableName;
    
    /**
     * Initializes the repository with DynamoDB client and table.
     */
    public AccountRepository() {
        this.tableName = System.getenv("ACCOUNT_TABLE");
        
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
        
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        
        this.accountTable = enhancedClient.table(tableName, TableSchema.fromBean(Account.class));
        logger.info("Initialized AccountRepository with table: {}", tableName);
    }
    
    /**
     * Finds an account by its ID.
     *
     * @param accountId The account ID to look up
     * @return Optional containing the account if found, empty otherwise
     */
    @Tracing
    public Optional<Account> findById(String accountId) {
        logger.info("Finding account by ID: {}", accountId);
        try {
            Key key = Key.builder().partitionValue(accountId).build();
            Account account = accountTable.getItem(key);
            return Optional.ofNullable(account);
        } catch (Exception e) {
            logger.error("Error finding account by ID: {}", accountId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Finds all accounts for a customer.
     *
     * @param customerId The customer ID to look up accounts for
     * @return List of accounts belonging to the customer
     */
    @Tracing
    public List<Account> findByCustomerId(String customerId) {
        logger.info("Finding accounts for customer ID: {}", customerId);
        try {
            QueryConditional queryConditional = QueryConditional
                    .keyEqualTo(Key.builder().partitionValue(customerId).build());
            
            List<Account> accounts = new ArrayList<>();
            accountTable.index("CustomerAccounts")
                    .query(queryConditional)
                    .forEach(page -> page.items().forEach(accounts::add));
            
            return accounts;
        } catch (Exception e) {
            logger.error("Error finding accounts for customer ID: {}", customerId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Saves an account to the database.
     *
     * @param account The account to save
     * @return The saved account
     */
    @Tracing
    public Account save(Account account) {
        logger.info("Saving account: {}", account);
        try {
            accountTable.putItem(account);
            return account;
        } catch (Exception e) {
            logger.error("Error saving account: {}", account, e);
            throw new RuntimeException("Failed to save account", e);
        }
    }
    
    /**
     * Deletes an account from the database.
     *
     * @param accountId The ID of the account to delete
     */
    @Tracing
    public void delete(String accountId) {
        logger.info("Deleting account with ID: {}", accountId);
        try {
            Key key = Key.builder().partitionValue(accountId).build();
            accountTable.deleteItem(key);
        } catch (Exception e) {
            logger.error("Error deleting account with ID: {}", accountId, e);
            throw new RuntimeException("Failed to delete account", e);
        }
    }
} 