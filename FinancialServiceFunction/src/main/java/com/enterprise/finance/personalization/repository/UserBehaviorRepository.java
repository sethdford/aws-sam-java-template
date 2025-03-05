package com.enterprise.finance.personalization.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.enterprise.finance.personalization.model.UserBehavior;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.lambda.powertools.tracing.Tracing;

/**
 * Repository class for UserBehavior entity operations with DynamoDB.
 */
public class UserBehaviorRepository {
    
    private static final Logger logger = LogManager.getLogger(UserBehaviorRepository.class);
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<UserBehavior> userBehaviorTable;
    private final String tableName;
    
    /**
     * Initializes the repository with DynamoDB client and table.
     */
    public UserBehaviorRepository() {
        this.tableName = System.getenv("USER_BEHAVIOR_TABLE");
        
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
        
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        
        this.userBehaviorTable = enhancedClient.table(tableName, TableSchema.fromBean(UserBehavior.class));
        logger.info("Initialized UserBehaviorRepository with table: {}", tableName);
    }
    
    /**
     * Finds a user behavior event by user ID and event ID.
     *
     * @param userId The user ID to look up
     * @param eventId The event ID to look up
     * @return Optional containing the user behavior if found, empty otherwise
     */
    @Tracing
    public Optional<UserBehavior> findByUserIdAndEventId(String userId, String eventId) {
        logger.info("Finding user behavior by user ID: {} and event ID: {}", userId, eventId);
        try {
            Key key = Key.builder()
                    .partitionValue(userId)
                    .sortValue(eventId)
                    .build();
            UserBehavior userBehavior = userBehaviorTable.getItem(key);
            return Optional.ofNullable(userBehavior);
        } catch (Exception e) {
            logger.error("Error finding user behavior by user ID: {} and event ID: {}", userId, eventId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Finds all behavior events for a user, ordered by event time.
     *
     * @param userId The user ID to look up events for
     * @param limit The maximum number of events to return
     * @return List of user behavior events
     */
    @Tracing
    public List<UserBehavior> findByUserIdOrderByEventTime(String userId, int limit) {
        logger.info("Finding user behaviors for user ID: {} with limit: {}", userId, limit);
        try {
            QueryConditional queryConditional = QueryConditional
                    .keyEqualTo(Key.builder().partitionValue(userId).build());
            
            QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                    .queryConditional(queryConditional)
                    .limit(limit)
                    .build();
            
            List<UserBehavior> behaviors = new ArrayList<>();
            userBehaviorTable.index("UserEventTimeIndex")
                    .query(request)
                    .forEach(page -> page.items().forEach(behaviors::add));
            
            return behaviors;
        } catch (Exception e) {
            logger.error("Error finding user behaviors for user ID: {}", userId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Saves a user behavior event to the database.
     *
     * @param userBehavior The user behavior to save
     * @return The saved user behavior
     */
    @Tracing
    public UserBehavior save(UserBehavior userBehavior) {
        logger.info("Saving user behavior: {}", userBehavior);
        try {
            userBehaviorTable.putItem(userBehavior);
            return userBehavior;
        } catch (Exception e) {
            logger.error("Error saving user behavior: {}", userBehavior, e);
            throw new RuntimeException("Failed to save user behavior", e);
        }
    }
    
    /**
     * Batch saves multiple user behavior events to the database.
     *
     * @param userBehaviors The list of user behaviors to save
     * @return The list of saved user behaviors
     */
    @Tracing
    public List<UserBehavior> batchSave(List<UserBehavior> userBehaviors) {
        logger.info("Batch saving {} user behaviors", userBehaviors.size());
        try {
            userBehaviors.forEach(this::save);
            return userBehaviors;
        } catch (Exception e) {
            logger.error("Error batch saving user behaviors", e);
            throw new RuntimeException("Failed to batch save user behaviors", e);
        }
    }
} 