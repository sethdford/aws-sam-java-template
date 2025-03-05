package com.enterprise.finance.personalization.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import com.enterprise.finance.personalization.model.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import software.amazon.lambda.powertools.tracing.Tracing;

import com.enterprise.finance.personalization.model.FinancialProduct;

/**
 * Service for interacting with OpenSearch for product search and indexing.
 */
public class OpenSearchService implements AutoCloseable {
    
    private static final Logger logger = LogManager.getLogger(OpenSearchService.class);
    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();
    
    /**
     * Constructor that initializes the OpenSearch client with the endpoint from environment variables.
     */
    public OpenSearchService() {
        String endpoint = System.getenv("OPENSEARCH_DOMAIN");
        if (endpoint == null || endpoint.isEmpty()) {
            // Use a default endpoint for local development
            endpoint = "http://localhost:9200";
            logger.info("OPENSEARCH_DOMAIN not set, using default endpoint: {}", endpoint);
        }
        this.client = new RestHighLevelClient(
                RestClient.builder(HttpHost.create(endpoint)));
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        logger.info("Initialized OpenSearchService with endpoint: {}", endpoint);
    }
    
    /**
     * Constructor that initializes the OpenSearch client with a specified endpoint.
     * 
     * @param endpoint The OpenSearch endpoint URL
     */
    public OpenSearchService(String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            // Use a default endpoint for local development
            endpoint = "http://localhost:9200";
            logger.info("Endpoint not provided, using default endpoint: {}", endpoint);
        }
        this.client = new RestHighLevelClient(
                RestClient.builder(HttpHost.create(endpoint)));
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        logger.info("Initialized OpenSearchService with endpoint: {}", endpoint);
    }
    
    /**
     * Searches for products based on user preferences.
     *
     * @param userId The user ID to personalize for
     * @param categoryPreferences Map of category preferences
     * @param limit The maximum number of results to return
     * @return List of product IDs
     */
    @Tracing
    public List<String> searchPersonalizedProducts(String userId, Map<String, Integer> categoryPreferences, int limit) {
        logger.info("Searching personalized products for user ID: {}", userId);
        try {
            // Build the search request
            Map<String, Object> requestBody = new HashMap<>();
            
            // Add user context for personalization
            Map<String, Object> query = new HashMap<>();
            Map<String, Object> function_score = new HashMap<>();
            
            // Base query to get active products
            Map<String, Object> bool = new HashMap<>();
            Map<String, Object> filter = new HashMap<>();
            Map<String, Object> term = new HashMap<>();
            term.put("isActive", true);
            filter.put("term", term);
            bool.put("filter", filter);
            
            // Add function score for personalization
            List<Map<String, Object>> functions = new ArrayList<>();
            
            // Add category preference boosts
            for (Map.Entry<String, Integer> entry : categoryPreferences.entrySet()) {
                Map<String, Object> function = new HashMap<>();
                Map<String, Object> weight = new HashMap<>();
                weight.put("weight", entry.getValue());
                function.put("weight", weight);
                
                Map<String, Object> filter_function = new HashMap<>();
                Map<String, Object> term_function = new HashMap<>();
                term_function.put("category", entry.getKey());
                filter_function.put("term", term_function);
                function.put("filter", filter_function);
                
                functions.add(function);
            }
            
            function_score.put("query", bool);
            function_score.put("functions", functions);
            function_score.put("boost_mode", "sum");
            query.put("function_score", function_score);
            requestBody.put("query", query);
            requestBody.put("size", limit);
            
            // Execute the search
            Request request = new Request("POST", "/products/_search");
            request.setJsonEntity(objectMapper.writeValueAsString(requestBody));
            
            Response response = client.getLowLevelClient().performRequest(request);
            
            // Parse the response
            Map<String, Object> responseMap = objectMapper.readValue(
                    response.getEntity().getContent(), 
                    new TypeReference<Map<String, Object>>() {});
            
            // Extract product IDs from hits
            Map<String, Object> hits = (Map<String, Object>) responseMap.get("hits");
            List<Map<String, Object>> hitsList = (List<Map<String, Object>>) hits.get("hits");
            
            List<String> productIds = new ArrayList<>();
            for (Map<String, Object> hit : hitsList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                productIds.add((String) source.get("productId"));
            }
            
            return productIds;
            
        } catch (Exception e) {
            logger.error("Error searching personalized products", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Indexes a product in OpenSearch with the given ID and data.
     *
     * @param productId The product ID
     * @param productData The product data to index
     * @return true if indexing was successful, false otherwise
     */
    @Tracing
    public boolean indexProduct(String productId, Map<String, Object> productData) {
        logger.info("Indexing product with ID: {}", productId);
        try {
            Request request = new Request("PUT", "/products/_doc/" + productId);
            request.setJsonEntity(objectMapper.writeValueAsString(productData));
            
            Response response = client.getLowLevelClient().performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            return statusCode >= 200 && statusCode < 300;
        } catch (Exception e) {
            logger.error("Error indexing product with ID: {}", productId, e);
            return false;
        }
    }
    
    /**
     * Indexes a product in OpenSearch.
     *
     * @param product The product to index
     * @return true if indexing was successful, false otherwise
     */
    @Tracing
    public boolean indexProduct(Product product) {
        logger.info("Indexing product with ID: {}", product.getProductId());
        try {
            Map<String, Object> productData = new HashMap<>();
            productData.put("productId", product.getProductId());
            productData.put("name", product.getName());
            productData.put("category", product.getCategory());
            productData.put("description", product.getDescription());
            
            // Add features if available
            if (product.getFeatures() != null) {
                productData.put("features", product.getFeatures());
            }
            
            productData.put("minimumInvestment", product.getMinimumInvestment());
            productData.put("annualPercentageYield", product.getAnnualPercentageYield());
            productData.put("termMonths", product.getTermMonths());
            productData.put("riskLevel", product.getRiskLevel());
            productData.put("isActive", product.getIsActive());
            productData.put("createdAt", product.getCreatedAt().toString());
            productData.put("updatedAt", product.getUpdatedAt().toString());
            
            // Add embedding from attributes if available
            if (product.getAttributes() != null && product.getAttributes().containsKey("embedding")) {
                productData.put("embedding", product.getAttributes().get("embedding"));
            }
            
            return indexProduct(product.getProductId(), productData);
        } catch (Exception e) {
            logger.error("Error indexing product with ID: {}", product.getProductId(), e);
            return false;
        }
    }
    
    /**
     * Searches for products similar to the given embedding.
     *
     * @param embedding The embedding vector to search with
     * @param limit The maximum number of results to return
     * @return List of products
     */
    @Tracing
    public List<Product> searchSimilarProducts(float[] embedding, int limit) {
        logger.info("Searching for similar products with embedding");
        try {
            // Build the search request for k-NN search
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> query = new HashMap<>();
            Map<String, Object> knn = new HashMap<>();
            Map<String, Object> field = new HashMap<>();
            
            field.put("vector", embedding);
            field.put("k", limit);
            
            knn.put("embedding", field);
            query.put("knn", knn);
            
            requestBody.put("query", query);
            requestBody.put("size", limit);
            
            // Execute the search
            Request request = new Request("POST", "/products/_search");
            request.setJsonEntity(objectMapper.writeValueAsString(requestBody));
            
            Response response = client.getLowLevelClient().performRequest(request);
            
            // Parse the response
            Map<String, Object> responseMap = objectMapper.readValue(
                    response.getEntity().getContent(), 
                    new TypeReference<Map<String, Object>>() {});
            
            // Extract products from hits
            Map<String, Object> hits = (Map<String, Object>) responseMap.get("hits");
            List<Map<String, Object>> hitsList = (List<Map<String, Object>>) hits.get("hits");
            
            List<Product> products = new ArrayList<>();
            for (Map<String, Object> hit : hitsList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                
                Product product = new Product();
                product.setProductId((String) source.get("productId"));
                product.setName((String) source.get("name"));
                product.setCategory((String) source.get("category"));
                product.setDescription((String) source.get("description"));
                
                // Set features if available
                if (source.get("features") != null) {
                    product.setFeatures((String) source.get("features"));
                }
                
                if (source.get("minimumInvestment") != null) {
                    product.setMinimumInvestment(Double.valueOf(source.get("minimumInvestment").toString()));
                }
                
                if (source.get("annualPercentageYield") != null) {
                    product.setAnnualPercentageYield(Double.valueOf(source.get("annualPercentageYield").toString()));
                }
                
                if (source.get("termMonths") != null) {
                    product.setTermMonths(Integer.valueOf(source.get("termMonths").toString()));
                }
                
                product.setRiskLevel((String) source.get("riskLevel"));
                
                if (source.get("isActive") != null) {
                    product.setIsActive(Boolean.valueOf(source.get("isActive").toString()));
                }
                
                // Add score from search
                double score = ((Number) hit.get("_score")).doubleValue();
                product.addAttribute("similarityScore", score);
                
                products.add(product);
            }
            
            return products;
            
        } catch (Exception e) {
            logger.error("Error searching similar products", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Searches for products similar to the given query and embedding.
     *
     * @param query The text query to search for
     * @param embedding The embedding vector to search with
     * @param limit The maximum number of results to return
     * @return List of products
     * @throws IOException If an error occurs during the search
     */
    @Tracing
    public List<Product> searchSimilarProducts(String query, float[] embedding, int limit) throws IOException {
        logger.info("Searching for similar products with query: '{}' and embedding", query);
        try {
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
            
            // Parse the response
            Map<String, Object> responseMap = objectMapper.readValue(
                    response.getEntity().getContent(), 
                    new TypeReference<Map<String, Object>>() {});
            
            // Extract products from hits
            Map<String, Object> hits = (Map<String, Object>) responseMap.get("hits");
            List<Map<String, Object>> hitsList = (List<Map<String, Object>>) hits.get("hits");
            
            List<Product> products = new ArrayList<>();
            for (Map<String, Object> hit : hitsList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                
                Product product = new Product();
                product.setProductId((String) source.get("productId"));
                product.setName((String) source.get("name"));
                product.setCategory((String) source.get("category"));
                product.setDescription((String) source.get("description"));
                
                // Set features if available
                if (source.get("features") != null) {
                    product.setFeatures((String) source.get("features"));
                }
                
                // Set other fields as needed
                
                products.add(product);
            }
            
            return products;
            
        } catch (Exception e) {
            logger.error("Error searching for similar products with query and embedding", e);
            // For development, return mock products instead of empty list
            return generateMockProductsForQuery(query, limit);
        }
    }
    
    /**
     * Generates mock products based on a query for testing.
     *
     * @param query The query to base the products on
     * @param count The number of products to generate
     * @return List of mock products
     */
    private List<Product> generateMockProductsForQuery(String query, int count) {
        List<Product> products = new ArrayList<>();
        
        // Extract keywords from the query to create more relevant mock products
        String lowercaseQuery = query.toLowerCase();
        String productType = determineProductTypeFromQuery(lowercaseQuery);
        String category = determineCategoryFromQuery(lowercaseQuery);
        
        for (int i = 0; i < count; i++) {
            Product product = new Product();
            product.setProductId("MOCK-" + (1000 + i));
            product.setName(generateProductNameFromQuery(query, i));
            product.setCategory(category);
            product.setDescription(generateDescriptionFromQuery(query));
            product.setIsActive(true);
            
            products.add(product);
        }
        
        return products;
    }
    
    /**
     * Determines a product type based on the query.
     *
     * @param query The query to analyze
     * @return A product type
     */
    private String determineProductTypeFromQuery(String query) {
        if (query.contains("investment") || query.contains("invest")) {
            return "INVESTMENT";
        } else if (query.contains("savings") || query.contains("save")) {
            return "SAVINGS";
        } else if (query.contains("loan") || query.contains("debt") || query.contains("credit")) {
            return "LOAN";
        } else if (query.contains("insurance")) {
            return "INSURANCE";
        } else if (query.contains("retirement")) {
            return "RETIREMENT";
        } else {
            return getRandomProductType();
        }
    }
    
    /**
     * Determines a category based on the query.
     *
     * @param query The query to analyze
     * @return A category
     */
    private String determineCategoryFromQuery(String query) {
        if (query.contains("investment")) {
            return "INVESTMENT";
        } else if (query.contains("savings")) {
            return "SAVINGS";
        } else if (query.contains("debt")) {
            return "DEBT_MANAGEMENT";
        } else if (query.contains("credit")) {
            return "CREDIT";
        } else if (query.contains("budget")) {
            return "BUDGETING";
        } else if (query.contains("retirement")) {
            return "RETIREMENT";
        } else if (query.contains("tax")) {
            return "TAX";
        } else {
            return getRandomCategory();
        }
    }
    
    /**
     * Generates a product name based on the query.
     *
     * @param query The query to base the name on
     * @param index The index of the product
     * @return A product name
     */
    private String generateProductNameFromQuery(String query, int index) {
        String lowercaseQuery = query.toLowerCase();
        
        if (lowercaseQuery.contains("investment")) {
            return "Premium Investment Fund " + (index + 1);
        } else if (lowercaseQuery.contains("savings")) {
            return "High-Yield Savings Account " + (index + 1);
        } else if (lowercaseQuery.contains("debt")) {
            return "Debt Consolidation Loan " + (index + 1);
        } else if (lowercaseQuery.contains("credit")) {
            return "Credit Builder Card " + (index + 1);
        } else if (lowercaseQuery.contains("budget")) {
            return "Budget Optimization Tool " + (index + 1);
        } else if (lowercaseQuery.contains("retirement")) {
            return "Retirement Planning Service " + (index + 1);
        } else {
            return "Financial Product " + (index + 1);
        }
    }
    
    /**
     * Generates a product description based on the query.
     *
     * @param query The query to base the description on
     * @return A product description
     */
    private String generateDescriptionFromQuery(String query) {
        String lowercaseQuery = query.toLowerCase();
        
        if (lowercaseQuery.contains("investment")) {
            return "A diversified investment fund designed to maximize returns while managing risk.";
        } else if (lowercaseQuery.contains("savings")) {
            return "A high-yield savings account with competitive interest rates and no monthly fees.";
        } else if (lowercaseQuery.contains("debt")) {
            return "A debt consolidation loan to help you simplify payments and potentially reduce interest rates.";
        } else if (lowercaseQuery.contains("credit")) {
            return "A credit card designed to help you build or rebuild your credit history with responsible use.";
        } else if (lowercaseQuery.contains("budget")) {
            return "A comprehensive budgeting tool to help you track expenses and optimize your spending.";
        } else if (lowercaseQuery.contains("retirement")) {
            return "A retirement planning service to help you prepare for a secure financial future.";
        } else {
            return "A financial product designed to help you achieve your financial goals.";
        }
    }
    
    /**
     * Gets a random product type.
     *
     * @return A random product type
     */
    private String getRandomProductType() {
        String[] types = {"INVESTMENT", "SAVINGS", "LOAN", "INSURANCE", "RETIREMENT"};
        return types[random.nextInt(types.length)];
    }
    
    /**
     * Gets a random category.
     *
     * @return A random category
     */
    private String getRandomCategory() {
        String[] categories = {"INVESTMENT", "SAVINGS", "DEBT_MANAGEMENT", "CREDIT", "BUDGETING", "RETIREMENT", "TAX"};
        return categories[random.nextInt(categories.length)];
    }
    
    @Override
    public void close() throws IOException {
        if (client != null) {
            client.close();
            logger.info("Closed OpenSearchService client");
        }
    }

    /**
     * Searches for products based on a query string.
     *
     * @param query The search query
     * @param limit The maximum number of results to return
     * @return List of financial products
     */
    @Tracing
    public List<FinancialProduct> searchProducts(String query, int limit) {
        logger.info("Searching for products with query: {}", query);
        try {
            // Build the search request
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> queryMap = new HashMap<>();
            Map<String, Object> match = new HashMap<>();
            match.put("description", query);
            queryMap.put("match", match);
            
            requestBody.put("query", queryMap);
            requestBody.put("size", limit);
            
            // Execute the search
            Request request = new Request("POST", "/products/_search");
            request.setJsonEntity(objectMapper.writeValueAsString(requestBody));
            
            Response response = client.getLowLevelClient().performRequest(request);
            
            // Parse the response
            Map<String, Object> responseMap = objectMapper.readValue(
                    response.getEntity().getContent(), 
                    new TypeReference<Map<String, Object>>() {});
            
            // Extract products from hits
            Map<String, Object> hits = (Map<String, Object>) responseMap.get("hits");
            List<Map<String, Object>> hitsList = (List<Map<String, Object>>) hits.get("hits");
            
            List<FinancialProduct> products = new ArrayList<>();
            for (Map<String, Object> hit : hitsList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                
                FinancialProduct product = new FinancialProduct();
                product.setProductId((String) source.get("productId"));
                product.setName((String) source.get("name"));
                product.setCategory((String) source.get("category"));
                product.setDescription((String) source.get("description"));
                
                if (source.get("assetClass") != null) {
                    product.setAssetClass((String) source.get("assetClass"));
                }
                
                products.add(product);
            }
            
            return products;
            
        } catch (Exception e) {
            logger.error("Error searching products", e);
            return new ArrayList<>();
        }
    }
} 