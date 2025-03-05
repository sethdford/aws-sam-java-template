package com.enterprise.finance.personalization.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

/**
 * Represents a financial product in the catalog.
 * This class is annotated for DynamoDB persistence and validation.
 */
@DynamoDbBean
public class Product {
    
    @NotBlank(message = "Product ID is required")
    private String productId;
    
    @NotBlank(message = "Product name is required")
    private String name;
    
    @NotBlank(message = "Product category is required")
    private String category;
    
    private String description;
    private Map<String, Object> attributes;
    private List<String> tags;
    private Double minimumInvestment;
    private Double annualPercentageYield;
    private Integer termMonths;
    private String riskLevel;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private String features;
    private float[] embedding;
    
    public Product() {
        // Required empty constructor for DynamoDB
        this.attributes = new HashMap<>();
        this.tags = new ArrayList<>();
        this.isActive = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    @DynamoDbPartitionKey
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @DynamoDbSecondaryPartitionKey(indexNames = {"CategoryIndex"})
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public Double getMinimumInvestment() {
        return minimumInvestment;
    }
    
    public void setMinimumInvestment(Double minimumInvestment) {
        this.minimumInvestment = minimumInvestment;
    }
    
    public Double getAnnualPercentageYield() {
        return annualPercentageYield;
    }
    
    public void setAnnualPercentageYield(Double annualPercentageYield) {
        this.annualPercentageYield = annualPercentageYield;
    }
    
    public Integer getTermMonths() {
        return termMonths;
    }
    
    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getFeatures() {
        return features;
    }
    
    public void setFeatures(String features) {
        this.features = features;
    }
    
    public float[] getEmbedding() {
        return embedding;
    }
    
    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
    
    /**
     * Adds an attribute to the product.
     *
     * @param key The attribute key
     * @param value The attribute value
     */
    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
        this.updatedAt = Instant.now();
    }
    
    /**
     * Adds a tag to the product.
     *
     * @param tag The tag to add
     */
    public void addTag(String tag) {
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
            this.updatedAt = Instant.now();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", attributes=" + attributes +
                ", tags=" + tags +
                ", minimumInvestment=" + minimumInvestment +
                ", annualPercentageYield=" + annualPercentageYield +
                ", termMonths=" + termMonths +
                ", riskLevel='" + riskLevel + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", features='" + features + '\'' +
                '}';
    }
} 