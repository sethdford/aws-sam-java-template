package com.enterprise.finance.personalization.repository;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.enterprise.finance.personalization.model.UserPreference;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.lambda.powertools.tracing.Tracing;

/**
 * Repository class for UserPreference entity operations with DynamoDB.
 */
public class UserPreferenceRepository {
    
    private static final Logger logger = LogManager.getLogger(UserPreferenceRepository.class);
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<UserPreference> userPreferenceTable;
    private final String tableName;
    
    /**
     * Initializes the repository with DynamoDB client and table.
     */
    public UserPreferenceRepository() {
        this.tableName = System.getenv("USER_PREFERENCES_TABLE");
        
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
        
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        
        this.userPreferenceTable = enhancedClient.table(tableName, TableSchema.fromBean(UserPreference.class));
        logger.info("Initialized UserPreferenceRepository with table: {}", tableName);
    }
    
    /**
     * Finds a user preference by user ID.
     *
     * @param userId The user ID to look up
     * @return Optional containing the user preference if found, empty otherwise
     */
    @Tracing
    public Optional<UserPreference> findByUserId(String userId) {
        logger.info("Finding user preference by user ID: {}", userId);
        try {
            Key key = Key.builder().partitionValue(userId).build();
            UserPreference userPreference = userPreferenceTable.getItem(key);
            return Optional.ofNullable(userPreference);
        } catch (Exception e) {
            logger.error("Error finding user preference by user ID: {}", userId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Saves a user preference to the database.
     *
     * @param userPreference The user preference to save
     * @return The saved user preference
     */
    @Tracing
    public UserPreference save(UserPreference userPreference) {
        logger.info("Saving user preference: {}", userPreference);
        try {
            userPreferenceTable.putItem(userPreference);
            return userPreference;
        } catch (Exception e) {
            logger.error("Error saving user preference: {}", userPreference, e);
            throw new RuntimeException("Failed to save user preference", e);
        }
    }
    
    /**
     * Deletes a user preference from the database.
     *
     * @param userId The ID of the user preference to delete
     */
    @Tracing
    public void delete(String userId) {
        logger.info("Deleting user preference with user ID: {}", userId);
        try {
            Key key = Key.builder().partitionValue(userId).build();
            userPreferenceTable.deleteItem(key);
        } catch (Exception e) {
            logger.error("Error deleting user preference with user ID: {}", userId, e);
            throw new RuntimeException("Failed to delete user preference", e);
        }
    }
} 