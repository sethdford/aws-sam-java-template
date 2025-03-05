package com.enterprise.finance.personalization.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * Represents user preferences for personalization.
 * This class is annotated for DynamoDB persistence and validation.
 */
@DynamoDbBean
public class UserPreference {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    private Map<String, Integer> categoryPreferences;
    private Map<String, Integer> productPreferences;
    private Map<String, String> settings;
    private Map<String, Object> attributes;
    private Instant lastUpdated;
    
    public UserPreference() {
        // Required empty constructor for DynamoDB
        this.categoryPreferences = new HashMap<>();
        this.productPreferences = new HashMap<>();
        this.settings = new HashMap<>();
        this.attributes = new HashMap<>();
        this.lastUpdated = Instant.now();
    }
    
    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Map<String, Integer> getCategoryPreferences() {
        return categoryPreferences;
    }
    
    public void setCategoryPreferences(Map<String, Integer> categoryPreferences) {
        this.categoryPreferences = categoryPreferences;
    }
    
    public Map<String, Integer> getProductPreferences() {
        return productPreferences;
    }
    
    public void setProductPreferences(Map<String, Integer> productPreferences) {
        this.productPreferences = productPreferences;
    }
    
    public Map<String, String> getSettings() {
        return settings;
    }
    
    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }
    
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    public Instant getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    /**
     * Updates a category preference score.
     *
     * @param category The category to update
     * @param score The preference score to add
     */
    public void updateCategoryPreference(String category, int score) {
        int currentScore = categoryPreferences.getOrDefault(category, 0);
        categoryPreferences.put(category, currentScore + score);
        this.lastUpdated = Instant.now();
    }
    
    /**
     * Updates a product preference score.
     *
     * @param productId The product ID to update
     * @param score The preference score to add
     */
    public void updateProductPreference(String productId, int score) {
        int currentScore = productPreferences.getOrDefault(productId, 0);
        productPreferences.put(productId, currentScore + score);
        this.lastUpdated = Instant.now();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPreference that = (UserPreference) o;
        return Objects.equals(userId, that.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
    
    @Override
    public String toString() {
        return "UserPreference{" +
                "userId='" + userId + '\'' +
                ", categoryPreferences=" + categoryPreferences +
                ", productPreferences=" + productPreferences +
                ", settings=" + settings +
                ", attributes=" + attributes +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
} 