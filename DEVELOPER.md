# Next Best Action Feature - Developer Guide

This guide provides technical details for developers working on the Next Best Action feature of the Enterprise Financial Services Personalization Platform.

## Overview

The Next Best Action feature analyzes a user's financial situation and provides personalized recommendations for financial actions they should take. It uses a combination of rule-based analysis and embedding-based similarity to generate and prioritize recommendations.

## Key Components

### 1. NextBestActionHandler

**Path**: `FinancialServiceFunction/src/main/java/com/enterprise/finance/personalization/NextBestActionHandler.java`

This Lambda function handler processes API requests for next best actions. It:
- Parses the request body to extract user information
- Creates default values for missing data
- Calls the NextBestActionService to generate recommendations
- Formats and returns the response

```java
@Override
@Tracing
@Logging(logEvent = true)
@Metrics(captureColdStart = true)
public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
    logger.info("Processing next best action request");
    
    try {
        // Parse the request body
        Map<String, Object> requestBody = objectMapper.readValue(input.getBody(), Map.class);
        
        // Extract user ID
        String userId = (String) requestBody.get("userId");
        if (userId == null || userId.isEmpty()) {
            return createErrorResponse(400, "Missing required field: userId");
        }
        
        // Extract or create user preferences
        UserPreferences userPreferences;
        if (requestBody.containsKey("userPreferences")) {
            userPreferences = objectMapper.convertValue(requestBody.get("userPreferences"), UserPreferences.class);
        } else {
            userPreferences = createDefaultUserPreferences(userId);
        }
        
        // Extract or create user behaviors
        List<UserBehavior> userBehaviors;
        if (requestBody.containsKey("userBehaviors")) {
            userBehaviors = objectMapper.convertValue(requestBody.get("userBehaviors"), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, UserBehavior.class));
        } else {
            userBehaviors = new ArrayList<>();
        }
        
        // Extract or create financial context
        FinancialContext financialContext;
        if (requestBody.containsKey("financialContext")) {
            financialContext = objectMapper.convertValue(requestBody.get("financialContext"), FinancialContext.class);
        } else {
            financialContext = createDefaultFinancialContext(userId);
        }
        
        // Extract max recommendations or use default
        int maxRecommendations = 5;
        if (requestBody.containsKey("maxRecommendations")) {
            maxRecommendations = (Integer) requestBody.get("maxRecommendations");
        }
        
        // Determine next best actions
        List<NextBestActionService.ActionRecommendation> recommendations = 
                nextBestActionService.determineNextBestActions(
                        userId, userPreferences, userBehaviors, financialContext, maxRecommendations);
        
        // Create response
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("userId", userId);
        responseBody.put("recommendations", recommendations);
        
        return createSuccessResponse(responseBody);
        
    } catch (JsonProcessingException e) {
        logger.error("Error processing request: {}", e.getMessage(), e);
        return createErrorResponse(400, "Invalid request format: " + e.getMessage());
    } catch (Exception e) {
        logger.error("Error determining next best actions: {}", e.getMessage(), e);
        return createErrorResponse(500, "Internal server error: " + e.getMessage());
    }
}
```

### 2. NextBestActionService

**Path**: `FinancialServiceFunction/src/main/java/com/enterprise/finance/personalization/service/NextBestActionService.java`

This service is responsible for analyzing user data and generating recommendations. It:
- Builds a text representation of the user profile
- Generates embeddings for the user profile
- Analyzes financial indicators to generate recommendations
- Enriches recommendations with relevant products
- Prioritizes recommendations based on confidence scores

```java
public List<ActionRecommendation> determineNextBestActions(
        String userId,
        UserPreferences userPreferences,
        List<UserBehavior> userBehaviors,
        FinancialContext financialContext,
        int maxRecommendations) {
    
    logger.info("Determining next best actions for user: {}", userId);
    
    try {
        // Build a text representation of the user profile for embedding
        String userProfileText = buildUserProfileText(userPreferences, userBehaviors, financialContext);
        
        // Generate embeddings for the user profile
        float[] userProfileEmbedding = bedrockService.generateEmbeddings(userProfileText);
        
        // Generate action recommendations based on financial indicators
        List<ActionRecommendation> recommendations = new ArrayList<>();
        
        // Add recommendations based on financial indicators
        recommendations.addAll(generateSavingsRecommendations(financialContext));
        recommendations.addAll(generateDebtManagementRecommendations(financialContext));
        recommendations.addAll(generateInvestmentRecommendations(userPreferences, financialContext));
        recommendations.addAll(generateCreditImprovementRecommendations(financialContext));
        
        // Find relevant products for each recommendation using embeddings
        enrichRecommendationsWithProducts(recommendations, userProfileEmbedding);
        
        // Sort recommendations by confidence score and limit to max requested
        return recommendations.stream()
                .sorted(Comparator.comparingDouble(ActionRecommendation::getConfidenceScore).reversed())
                .limit(maxRecommendations)
                .collect(Collectors.toList());
    } catch (IOException e) {
        logger.error("Error determining next best actions: {}", e.getMessage(), e);
        return Collections.emptyList();
    }
}
```

### 3. BedrockService (Mock Implementation)

**Path**: `FinancialServiceFunction/src/main/java/com/enterprise/finance/personalization/service/BedrockService.java`

This service generates embeddings from text. In the current implementation, it provides a mock implementation that generates random embeddings.

```java
public float[] generateEmbeddings(String text) {
    logger.info("Generating mock embeddings for text of length: {}", text.length());
    
    // Generate random embeddings
    float[] embeddings = new float[EMBEDDING_DIMENSION];
    for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
        embeddings[i] = random.nextFloat() * 2 - 1; // Random value between -1 and 1
    }
    
    // Normalize the embeddings
    float norm = 0.0f;
    for (float val : embeddings) {
        norm += val * val;
    }
    norm = (float) Math.sqrt(norm);
    
    for (int i = 0; i < embeddings.length; i++) {
        embeddings[i] /= norm;
    }
    
    logger.info("Successfully generated mock embeddings");
    return embeddings;
}
```

### 4. MockOpenSearchService

**Path**: `FinancialServiceFunction/src/main/java/com/enterprise/finance/personalization/service/MockOpenSearchService.java`

This service provides a simulated OpenSearch experience for development. It generates mock products based on search queries.

```java
@Tracing
public List<FinancialProduct> searchSimilarProducts(String query, float[] embedding, int limit) throws IOException {
    logger.info("Mock searching for similar products with query: '{}' and embedding (limit: {})", query, limit);
    return generateMockProductsForQuery(query, limit);
}

private List<FinancialProduct> generateMockProductsForQuery(String query, int count) {
    List<FinancialProduct> products = new ArrayList<>();
    
    // Extract keywords from the query to create more relevant mock products
    String lowercaseQuery = query.toLowerCase();
    String productType = determineProductTypeFromQuery(lowercaseQuery);
    String category = determineCategoryFromQuery(lowercaseQuery);
    
    for (int i = 0; i < count; i++) {
        FinancialProduct product = new FinancialProduct();
        product.setProductId("PROD-" + (1000 + i));
        product.setProductName(generateProductNameFromQuery(query, i));
        product.setProductType(productType);
        product.setCategory(category);
        product.setDescription(generateDescriptionFromQuery(query));
        product.setInterestRate(new BigDecimal(String.format("%.2f", 1 + random.nextDouble() * 5)));
        product.setCreatedDate(Instant.now());
        product.setLastUpdated(Instant.now());
        product.setActive(true);
        
        // Add some tags
        product.addTag(category);
        product.addTag(productType);
        
        // Add some benefits
        product.addBenefit("24/7 customer support");
        product.addBenefit("Mobile app access");
        
        products.add(product);
    }
    
    return products;
}
```

## Data Models

### 1. UserPreferences

**Path**: `FinancialServiceFunction/src/main/java/com/enterprise/finance/personalization/model/UserPreferences.java`

This model stores user preference data for personalization.

```java
@DynamoDbBean
public class UserPreferences {
    private String userId;
    private int riskTolerance;
    private String investmentHorizon;
    private List<String> preferredCategories;
    private Map<String, Integer> categoryPreferences;
    private Map<String, Object> attributes;

    // Getters and setters...
    
    public void addCategoryPreference(String category, int weight) {
        this.categoryPreferences.put(category, weight);
        if (weight > 0 && !this.preferredCategories.contains(category)) {
            this.preferredCategories.add(category);
        }
    }
}
```

### 2. UserBehavior

**Path**: `FinancialServiceFunction/src/main/java/com/enterprise/finance/personalization/model/UserBehavior.java`

This model tracks user interactions and behaviors.

```java
@DynamoDbBean
public class UserBehavior {
    private String userId;
    private String behaviorId;
    private String actionType;
    private String resourceType;
    private String resourceId;
    private Instant timestamp;
    private Map<String, Object> metadata;

    // Getters and setters...
    
    public static UserBehavior createViewBehavior(String userId, String resourceType, String resourceId) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setBehaviorId(generateBehaviorId("VIEW", resourceType, resourceId));
        behavior.setActionType("VIEW");
        behavior.setResourceType(resourceType);
        behavior.setResourceId(resourceId);
        return behavior;
    }
}
```

### 3. FinancialContext

**Path**: `FinancialServiceFunction/src/main/java/com/enterprise/finance/personalization/model/FinancialContext.java`

This model represents a user's financial situation.

```java
@DynamoDbBean
public class FinancialContext {
    private String userId;
    private BigDecimal monthlyIncome;
    private BigDecimal totalSavings;
    private BigDecimal totalDebt;
    private BigDecimal creditScore;
    private BigDecimal monthlyExpenses;
    private BigDecimal disposableIncome;
    private BigDecimal savingsRate;
    private BigDecimal debtToIncomeRatio;
    private List<Account> accounts;
    private List<FinancialGoal> goals;
    private Map<String, Object> additionalAttributes;
    private Instant lastUpdated;

    // Getters and setters...
    
    @DynamoDbBean
    public static class Account {
        private String accountId;
        private String accountType;
        private String accountName;
        private BigDecimal balance;
        private BigDecimal interestRate;
        private Instant lastUpdated;
        
        // Getters and setters...
    }
    
    @DynamoDbBean
    public static class FinancialGoal {
        private String goalId;
        private String goalType;
        private String description;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount;
        private Instant targetDate;
        private Instant createdDate;
        private String status;
        
        // Getters and setters...
    }
}
```

### 4. FinancialProduct

**Path**: `FinancialServiceFunction/src/main/java/com/enterprise/finance/personalization/model/FinancialProduct.java`

This model represents financial products that can be recommended.

```java
@DynamoDbBean
public class FinancialProduct {
    private String productId;
    private String productName;
    private String productType;
    private String description;
    private String category;
    private List<String> tags;
    private Map<String, String> attributes;
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

    // Getters and setters...
    
    public String toEmbeddingText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Product Name: ").append(productName).append("\n");
        sb.append("Product Type: ").append(productType).append("\n");
        sb.append("Category: ").append(category).append("\n");
        sb.append("Description: ").append(description).append("\n");
        
        // Add other fields...
        
        return sb.toString();
    }
}
```

## Recommendation Generation Logic

The NextBestActionService implements several recommendation generators:

### 1. Savings Recommendations

```java
private List<ActionRecommendation> generateSavingsRecommendations(FinancialContext context) {
    List<ActionRecommendation> recommendations = new ArrayList<>();
    
    if (context == null) {
        return recommendations;
    }
    
    // Check if savings rate is below 20%
    if (context.getSavingsRate() != null && context.getSavingsRate().compareTo(new BigDecimal("0.2")) < 0) {
        double confidenceScore = 0.9 - (context.getSavingsRate().doubleValue() * 2); // Higher confidence for lower savings rates
        
        recommendations.add(new ActionRecommendation(
            ActionType.SAVINGS_RECOMMENDATION,
            "Increase your monthly savings rate to build a stronger financial foundation.",
            confidenceScore,
            "Your current savings rate is " + context.getSavingsRate().multiply(new BigDecimal("100")) + "%, " +
            "which is below the recommended 20% threshold.",
            new ArrayList<>()
        ));
    }
    
    // Check if emergency fund is insufficient (less than 3 months of expenses)
    if (context.getTotalSavings() != null && context.getMonthlyExpenses() != null) {
        BigDecimal emergencyFundTarget = context.getMonthlyExpenses().multiply(new BigDecimal("3"));
        if (context.getTotalSavings().compareTo(emergencyFundTarget) < 0) {
            double monthsCovered = context.getTotalSavings().divide(context.getMonthlyExpenses(), 2, java.math.RoundingMode.HALF_UP).doubleValue();
            double confidenceScore = 0.95 - (monthsCovered / 6.0); // Higher confidence for lower coverage
            
            recommendations.add(new ActionRecommendation(
                ActionType.SAVINGS_RECOMMENDATION,
                "Build an emergency fund covering at least 3-6 months of expenses.",
                confidenceScore,
                "Your current savings would cover approximately " + String.format("%.1f", monthsCovered) + 
                " months of expenses, which is below the recommended 3-6 month threshold.",
                new ArrayList<>()
            ));
        }
    }
    
    return recommendations;
}
```

### 2. Debt Management Recommendations

```java
private List<ActionRecommendation> generateDebtManagementRecommendations(FinancialContext context) {
    List<ActionRecommendation> recommendations = new ArrayList<>();
    
    if (context == null) {
        return recommendations;
    }
    
    // Check if debt-to-income ratio is high (above 36%)
    if (context.getDebtToIncomeRatio() != null && context.getDebtToIncomeRatio().compareTo(new BigDecimal("0.36")) > 0) {
        double confidenceScore = Math.min(0.95, context.getDebtToIncomeRatio().doubleValue() * 1.5); // Higher confidence for higher DTI
        
        recommendations.add(new ActionRecommendation(
            ActionType.DEBT_MANAGEMENT,
            "Reduce your debt-to-income ratio to improve financial health.",
            confidenceScore,
            "Your current debt-to-income ratio is " + context.getDebtToIncomeRatio().multiply(new BigDecimal("100")) + "%, " +
            "which is above the recommended 36% threshold.",
            new ArrayList<>()
        ));
    }
    
    return recommendations;
}
```

### 3. Investment Recommendations

```java
private List<ActionRecommendation> generateInvestmentRecommendations(UserPreferences preferences, FinancialContext context) {
    List<ActionRecommendation> recommendations = new ArrayList<>();
    
    if (preferences == null || context == null) {
        return recommendations;
    }
    
    // Check if user has sufficient disposable income but limited investments
    if (context.getDisposableIncome() != null && context.getDisposableIncome().compareTo(new BigDecimal("500")) > 0) {
        boolean hasInvestmentAccounts = false;
        
        if (context.getAccounts() != null) {
            for (FinancialContext.Account account : context.getAccounts()) {
                if (account.getAccountType() != null && 
                    (account.getAccountType().contains("INVESTMENT") || 
                     account.getAccountType().contains("RETIREMENT"))) {
                    hasInvestmentAccounts = true;
                    break;
                }
            }
        }
        
        if (!hasInvestmentAccounts) {
            recommendations.add(new ActionRecommendation(
                ActionType.INVESTMENT_OPPORTUNITY,
                "Start investing to grow your wealth and prepare for the future.",
                0.85,
                "You have " + context.getDisposableIncome() + " in monthly disposable income " +
                "that could be partially allocated to investments.",
                new ArrayList<>()
            ));
        }
    }
    
    return recommendations;
}
```

### 4. Credit Improvement Recommendations

```java
private List<ActionRecommendation> generateCreditImprovementRecommendations(FinancialContext context) {
    List<ActionRecommendation> recommendations = new ArrayList<>();
    
    if (context == null || context.getCreditScore() == null) {
        return recommendations;
    }
    
    // Check if credit score is below 700
    if (context.getCreditScore().compareTo(new BigDecimal("700")) < 0) {
        double confidenceScore = 0.9 - (context.getCreditScore().doubleValue() / 1000.0); // Higher confidence for lower scores
        
        recommendations.add(new ActionRecommendation(
            ActionType.CREDIT_IMPROVEMENT,
            "Improve your credit score to qualify for better financial products.",
            confidenceScore,
            "Your current credit score is " + context.getCreditScore() + ", " +
            "which is below the 'good' threshold of 700.",
            new ArrayList<>()
        ));
    }
    
    return recommendations;
}
```

## Product Enrichment

The NextBestActionService enriches recommendations with relevant products using the OpenSearchService:

```java
private void enrichRecommendationsWithProducts(List<ActionRecommendation> recommendations, float[] userProfileEmbedding) throws IOException {
    for (ActionRecommendation recommendation : recommendations) {
        try {
            // Create a search query combining the recommendation type and user profile
            String searchQuery = recommendation.getActionType().getDisplayName() + " " + recommendation.getDescription();
            
            // Search for relevant products
            List<FinancialProduct> relevantProducts = openSearchService.searchSimilarProducts(searchQuery, userProfileEmbedding, 3);
            
            // Add the products to the recommendation
            for (FinancialProduct product : relevantProducts) {
                ((List<FinancialProduct>)recommendation.getRelatedProducts()).add(product);
            }
            
            logger.debug("Added {} relevant products to recommendation: {}", 
                       recommendation.getRelatedProducts().size(), recommendation.getActionType());
        } catch (Exception e) {
            logger.error("Error enriching recommendation with products: {}", e.getMessage(), e);
        }
    }
}
```

## Testing

### Local Testing

To test the Next Best Action feature locally:

1. Build the project:
   ```
   mvn clean package
   sam build
   ```

2. Run the test script:
   ```
   ./test-next-best-action.sh
   ```

3. Alternatively, invoke the function directly:
   ```
   sam local invoke NextBestActionFunction --event events/next-best-action-detailed-event.json
   ```

### Test Events

Two test events are provided:

1. **Basic Test Event**: `events/next-best-action-event.json`
   - Contains only user ID and max recommendations
   - Uses default values for other parameters

2. **Detailed Test Event**: `events/next-best-action-detailed-event.json`
   - Contains complete user profile data
   - Includes financial context, user preferences, and behaviors

## Extending the Feature

### Adding New Recommendation Types

To add a new recommendation type:

1. Add the new type to the `ActionType` enum in `NextBestActionService.java`:
   ```java
   public enum ActionType {
       // Existing types...
       NEW_RECOMMENDATION_TYPE("New Recommendation Type");
       
       // Rest of the enum...
   }
   ```

2. Create a new recommendation generator method:
   ```java
   private List<ActionRecommendation> generateNewRecommendations(UserPreferences preferences, FinancialContext context) {
       List<ActionRecommendation> recommendations = new ArrayList<>();
       
       // Implement recommendation logic
       
       return recommendations;
   }
   ```

3. Add the new generator to the `determineNextBestActions` method:
   ```java
   recommendations.addAll(generateNewRecommendations(userPreferences, financialContext));
   ```

### Implementing Real Embedding Service

To replace the mock BedrockService with a real implementation:

1. Update the `BedrockService.java` file to use the AWS SDK for Bedrock:
   ```java
   public float[] generateEmbeddings(String text) {
       try {
           // Create request
           SdkBytes inputBytes = SdkBytes.fromUtf8String(
               "{\"inputText\": \"" + text.replace("\"", "\\\"") + "\"}"
           );
           
           InvokeModelRequest request = InvokeModelRequest.builder()
               .modelId(modelId)
               .contentType("application/json")
               .accept("application/json")
               .body(inputBytes)
               .build();
           
           // Invoke model
           InvokeModelResponse response = bedrockRuntimeClient.invokeModel(request);
           
           // Parse response
           String responseBody = response.body().asUtf8String();
           Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
           List<Float> embedding = (List<Float>) responseMap.get("embedding");
           
           // Convert to float array
           float[] result = new float[embedding.size()];
           for (int i = 0; i < embedding.size(); i++) {
               result[i] = embedding.get(i);
           }
           
           return result;
       } catch (Exception e) {
           logger.error("Error generating embeddings: {}", e.getMessage(), e);
           throw new RuntimeException("Failed to generate embeddings", e);
       }
   }
   ```

2. Update the constructor to initialize the Bedrock client:
   ```java
   public BedrockService() {
       this.modelId = System.getenv("BEDROCK_MODEL_ID");
       this.bedrockRuntimeClient = BedrockRuntimeClient.builder()
           .region(Region.of(System.getenv("AWS_REGION")))
           .credentialsProvider(DefaultCredentialsProvider.create())
           .build();
       logger.info("Initialized BedrockService with model ID: {}", modelId);
   }
   ```

### Implementing Real OpenSearch Service

To replace the mock OpenSearchService with a real implementation:

1. Update the `searchSimilarProducts` method in `OpenSearchService.java`:
   ```java
   public List<FinancialProduct> searchSimilarProducts(String query, float[] embedding, int limit) throws IOException {
       // Build the search request combining text search and vector search
       Map<String, Object> requestBody = new HashMap<>();
       
       Map<String, Object> boolQuery = new HashMap<>();
       List<Map<String, Object>> should = new ArrayList<>();
       
       // Add text search component
       Map<String, Object> matchQuery = new HashMap<>();
       Map<String, Object> match = new HashMap<>();
       match.put("description", query);
       matchQuery.put("match", match);
       should.add(matchQuery);
       
       // Add kNN search component
       Map<String, Object> knnQuery = new HashMap<>();
       Map<String, Object> knn = new HashMap<>();
       Map<String, Object> field = new HashMap<>();
       
       field.put("vector", embedding);
       field.put("k", limit);
       
       knn.put("embedding", field);
       knnQuery.put("knn", knn);
       should.add(knnQuery);
       
       boolQuery.put("should", should);
       Map<String, Object> query_map = new HashMap<>();
       query_map.put("bool", boolQuery);
       
       requestBody.put("query", query_map);
       requestBody.put("size", limit);
       
       // Execute the search
       Request request = new Request("POST", "/products/_search");
       request.setJsonEntity(objectMapper.writeValueAsString(requestBody));
       
       Response response = client.getLowLevelClient().performRequest(request);
       
       // Parse the response and convert to FinancialProduct objects
       // ...
       
       return products;
   }
   ```

## Troubleshooting

### Common Issues

1. **Missing Environment Variables**:
   - Ensure that all required environment variables are set in the SAM template.
   - Required variables: `BEDROCK_MODEL_ID`, `OPENSEARCH_DOMAIN`, `USER_PREFERENCES_TABLE`, `USER_BEHAVIOR_TABLE`, `PRODUCT_CATALOG_TABLE`.

2. **DynamoDB Access Issues**:
   - Verify that the Lambda function has the correct IAM permissions to access DynamoDB tables.
   - Check that the table names in the environment variables match the actual table names.

3. **OpenSearch Connection Issues**:
   - Ensure that the OpenSearch domain endpoint is correct.
   - Verify that the Lambda function has the correct IAM permissions to access OpenSearch.

4. **Bedrock Service Issues**:
   - Check that the Bedrock model ID is correct.
   - Verify that the Lambda function has the correct IAM permissions to invoke Bedrock models.

### Logging

The application uses structured logging with the AWS Lambda Powertools library:

```java
@Logging(logEvent = true)
@Metrics(captureColdStart = true)
@Tracing
public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
    // Function implementation
}
```

To view logs:
1. In the AWS Console, navigate to CloudWatch Logs.
2. Find the log group for the Lambda function: `/aws/lambda/NextBestActionFunction`.
3. Search for specific log entries using the search feature.

### Debugging

For local debugging:

1. Use the SAM CLI with the `--debug` flag:
   ```
   sam local invoke NextBestActionFunction --event events/next-best-action-event.json --debug
   ```

2. Add debug log statements to the code:
   ```java
   logger.debug("User profile text: {}", userProfileText);
   ```

3. Set the log level in the environment variables:
   ```
   LOG_LEVEL: DEBUG
   ```

## Performance Considerations

1. **Lambda Memory Allocation**:
   - The function is configured with 1024 MB of memory.
   - Increase memory allocation if processing large user profiles or many recommendations.

2. **Timeout Configuration**:
   - The function timeout is set to 30 seconds.
   - Adjust the timeout if processing complex financial contexts or many recommendations.

3. **Caching**:
   - Consider implementing caching for frequently accessed data.
   - Use DAX for DynamoDB caching to improve read performance.

4. **Batch Processing**:
   - For bulk recommendation generation, consider implementing a batch processing approach.
   - Use Step Functions for orchestrating complex recommendation workflows.

## Conclusion

The Next Best Action feature provides a powerful way to deliver personalized financial recommendations to users. By following this developer guide, you should be able to understand, extend, and troubleshoot the feature effectively. 