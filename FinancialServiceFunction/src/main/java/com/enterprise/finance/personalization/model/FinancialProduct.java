package com.enterprise.finance.personalization.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * Model class representing a financial product that can be recommended to users.
 */
@DynamoDbBean
public class FinancialProduct {
    private String productId;
    private String name;
    private String description;
    private String category;
    private String assetClass;
    private BigDecimal currentPrice;
    private BigDecimal purchasePrice;
    private Instant purchaseDate;
    private BigDecimal quantity;
    private Double expenseRatio;
    private Double yield;
    private boolean taxInefficient;
    private Map<String, Object> attributes;
    private String productType;
    private List<String> tags;
    private BigDecimal interestRate;
    private BigDecimal annualFee;
    private BigDecimal minimumDeposit;
    private BigDecimal maximumLimit;
    private String riskLevel;
    private List<String> eligibilityCriteria;
    private List<String> benefits;
    private List<String> requirements;
    private Instant createdDate;
    private Instant lastUpdated;
    private boolean isActive;
    private float[] embeddings;

    public FinancialProduct() {
        this.tags = new ArrayList<>();
        this.attributes = new HashMap<>();
        this.eligibilityCriteria = new ArrayList<>();
        this.benefits = new ArrayList<>();
        this.requirements = new ArrayList<>();
        this.createdDate = Instant.now();
        this.lastUpdated = Instant.now();
        this.isActive = true;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(String assetClass) {
        this.assetClass = assetClass;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Instant getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Instant purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Double getExpenseRatio() {
        return expenseRatio;
    }

    public void setExpenseRatio(Double expenseRatio) {
        this.expenseRatio = expenseRatio;
    }

    public Double getYield() {
        return yield;
    }

    public void setYield(Double yield) {
        this.yield = yield;
    }

    public boolean isTaxInefficient() {
        return taxInefficient;
    }

    public void setTaxInefficient(boolean taxInefficient) {
        this.taxInefficient = taxInefficient;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getAnnualFee() {
        return annualFee;
    }

    public void setAnnualFee(BigDecimal annualFee) {
        this.annualFee = annualFee;
    }

    public BigDecimal getMinimumDeposit() {
        return minimumDeposit;
    }

    public void setMinimumDeposit(BigDecimal minimumDeposit) {
        this.minimumDeposit = minimumDeposit;
    }

    public BigDecimal getMaximumLimit() {
        return maximumLimit;
    }

    public void setMaximumLimit(BigDecimal maximumLimit) {
        this.maximumLimit = maximumLimit;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<String> getEligibilityCriteria() {
        return eligibilityCriteria;
    }

    public void setEligibilityCriteria(List<String> eligibilityCriteria) {
        this.eligibilityCriteria = eligibilityCriteria;
    }

    public void addEligibilityCriterion(String criterion) {
        this.eligibilityCriteria.add(criterion);
    }

    public List<String> getBenefits() {
        return benefits;
    }

    public void setBenefits(List<String> benefits) {
        this.benefits = benefits;
    }

    public void addBenefit(String benefit) {
        this.benefits.add(benefit);
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }

    public void addRequirement(String requirement) {
        this.requirements.add(requirement);
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public float[] getEmbeddings() {
        return embeddings;
    }

    public void setEmbeddings(float[] embeddings) {
        this.embeddings = embeddings;
    }

    /**
     * Creates a text representation of the product for embedding generation.
     *
     * @return A string representation of the product
     */
    public String toEmbeddingText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Product Name: ").append(name).append("\n");
        sb.append("Product Type: ").append(productType).append("\n");
        sb.append("Category: ").append(category).append("\n");
        sb.append("Description: ").append(description).append("\n");
        
        if (!tags.isEmpty()) {
            sb.append("Tags: ").append(String.join(", ", tags)).append("\n");
        }
        
        if (interestRate != null) {
            sb.append("Interest Rate: ").append(interestRate).append("\n");
        }
        
        if (annualFee != null) {
            sb.append("Annual Fee: ").append(annualFee).append("\n");
        }
        
        if (minimumDeposit != null) {
            sb.append("Minimum Deposit: ").append(minimumDeposit).append("\n");
        }
        
        if (maximumLimit != null) {
            sb.append("Maximum Limit: ").append(maximumLimit).append("\n");
        }
        
        if (riskLevel != null) {
            sb.append("Risk Level: ").append(riskLevel).append("\n");
        }
        
        if (!benefits.isEmpty()) {
            sb.append("Benefits:\n");
            for (String benefit : benefits) {
                sb.append("- ").append(benefit).append("\n");
            }
        }
        
        if (!eligibilityCriteria.isEmpty()) {
            sb.append("Eligibility Criteria:\n");
            for (String criterion : eligibilityCriteria) {
                sb.append("- ").append(criterion).append("\n");
            }
        }
        
        return sb.toString();
    }
} 