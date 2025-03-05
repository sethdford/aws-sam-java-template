package com.enterprise.finance.personalization.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * Model class representing user preferences for personalization.
 */
@DynamoDbBean
public class UserPreferences {
    private String userId;
    private int riskTolerance;
    private String investmentHorizon;
    private List<String> preferredCategories;
    private Map<String, Integer> categoryPreferences;
    private Map<String, Object> attributes;

    public UserPreferences() {
        this.preferredCategories = new ArrayList<>();
        this.categoryPreferences = new HashMap<>();
        this.attributes = new HashMap<>();
    }

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRiskTolerance() {
        return riskTolerance;
    }

    public void setRiskTolerance(int riskTolerance) {
        this.riskTolerance = riskTolerance;
    }

    public String getInvestmentHorizon() {
        return investmentHorizon;
    }

    public void setInvestmentHorizon(String investmentHorizon) {
        this.investmentHorizon = investmentHorizon;
    }

    public List<String> getPreferredCategories() {
        return preferredCategories;
    }

    public void setPreferredCategories(List<String> preferredCategories) {
        this.preferredCategories = preferredCategories;
    }

    public Map<String, Integer> getCategoryPreferences() {
        return categoryPreferences;
    }

    public void setCategoryPreferences(Map<String, Integer> categoryPreferences) {
        this.categoryPreferences = categoryPreferences;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * Adds a category preference with the specified weight.
     *
     * @param category The product category
     * @param weight The preference weight (higher values indicate stronger preference)
     */
    public void addCategoryPreference(String category, int weight) {
        this.categoryPreferences.put(category, weight);
        if (weight > 0 && !this.preferredCategories.contains(category)) {
            this.preferredCategories.add(category);
        }
    }

    /**
     * Adds an attribute to the user preferences.
     *
     * @param key The attribute key
     * @param value The attribute value
     */
    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }
} 