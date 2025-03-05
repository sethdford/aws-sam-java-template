package com.enterprise.finance.personalization.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Model class representing user behavior and interactions.
 */
@DynamoDbBean
public class UserBehavior {
    private String userId;
    private String behaviorId;
    private String actionType;
    private String resourceType;
    private String resourceId;
    private Instant timestamp;
    private Map<String, Object> metadata;

    public UserBehavior() {
        this.timestamp = Instant.now();
        this.metadata = new HashMap<>();
    }

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @DynamoDbSortKey
    public String getBehaviorId() {
        return behaviorId;
    }

    public void setBehaviorId(String behaviorId) {
        this.behaviorId = behaviorId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Adds metadata to the behavior record.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * Creates a new behavior record for a view action.
     *
     * @param userId The user ID
     * @param resourceType The type of resource viewed
     * @param resourceId The ID of the resource viewed
     * @return A new UserBehavior instance
     */
    public static UserBehavior createViewBehavior(String userId, String resourceType, String resourceId) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setBehaviorId(generateBehaviorId("VIEW", resourceType, resourceId));
        behavior.setActionType("VIEW");
        behavior.setResourceType(resourceType);
        behavior.setResourceId(resourceId);
        return behavior;
    }

    /**
     * Creates a new behavior record for a click action.
     *
     * @param userId The user ID
     * @param resourceType The type of resource clicked
     * @param resourceId The ID of the resource clicked
     * @return A new UserBehavior instance
     */
    public static UserBehavior createClickBehavior(String userId, String resourceType, String resourceId) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setBehaviorId(generateBehaviorId("CLICK", resourceType, resourceId));
        behavior.setActionType("CLICK");
        behavior.setResourceType(resourceType);
        behavior.setResourceId(resourceId);
        return behavior;
    }

    /**
     * Creates a new behavior record for a purchase action.
     *
     * @param userId The user ID
     * @param resourceType The type of resource purchased
     * @param resourceId The ID of the resource purchased
     * @return A new UserBehavior instance
     */
    public static UserBehavior createPurchaseBehavior(String userId, String resourceType, String resourceId) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setBehaviorId(generateBehaviorId("PURCHASE", resourceType, resourceId));
        behavior.setActionType("PURCHASE");
        behavior.setResourceType(resourceType);
        behavior.setResourceId(resourceId);
        return behavior;
    }

    private static String generateBehaviorId(String actionType, String resourceType, String resourceId) {
        return String.format("%s_%s_%s_%d", actionType, resourceType, resourceId, Instant.now().toEpochMilli());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserBehavior that = (UserBehavior) o;
        return Objects.equals(userId, that.userId) && Objects.equals(behaviorId, that.behaviorId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, behaviorId);
    }
    
    @Override
    public String toString() {
        return "UserBehavior{" +
                "userId='" + userId + '\'' +
                ", behaviorId='" + behaviorId + '\'' +
                ", actionType='" + actionType + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", timestamp=" + timestamp +
                ", metadata=" + metadata +
                '}';
    }
} 