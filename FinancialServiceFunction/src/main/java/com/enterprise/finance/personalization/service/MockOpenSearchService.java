package com.enterprise.finance.personalization.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enterprise.finance.personalization.model.FinancialProduct;
import com.enterprise.finance.personalization.model.Product;

import software.amazon.lambda.powertools.tracing.Tracing;

/**
 * Mock implementation of OpenSearch service for development and testing.
 * This class provides simulated responses for product searches without requiring
 * an actual OpenSearch instance.
 */
public class MockOpenSearchService extends OpenSearchService {
    private static final Logger logger = LoggerFactory.getLogger(MockOpenSearchService.class);
    private final Random random = new Random();
    
    /**
     * Constructor for MockOpenSearchService.
     */
    public MockOpenSearchService() {
        super((String)null); // Pass null to use default endpoint
        logger.info("Initializing mock OpenSearchService");
    }
    
    /**
     * Constructor for MockOpenSearchService.
     *
     * @param domainEndpoint The OpenSearch domain endpoint (ignored in mock implementation)
     */
    public MockOpenSearchService(String domainEndpoint) {
        super(domainEndpoint);
        logger.info("Initializing mock OpenSearchService with endpoint: {}", domainEndpoint);
    }
    
    /**
     * Searches for products similar to the given embedding.
     *
     * @param embedding The embedding to search for
     * @param limit The maximum number of results to return
     * @return A list of similar products
     */
    @Tracing
    public List<Product> searchSimilarProducts(float[] embedding, int limit) {
        logger.info("Mock searching for similar products with embedding (limit: {})", limit);
        return convertToProducts(generateMockProducts(limit));
    }
    
    /**
     * Searches for products similar to the given query and embedding.
     *
     * @param query The text query to search for
     * @param embedding The embedding to search for
     * @param limit The maximum number of results to return
     * @return A list of similar products
     */
    @Tracing
    public List<Product> searchSimilarProducts(String query, float[] embedding, int limit) {
        logger.info("Mock searching for similar products with query: '{}' and embedding (limit: {})", query, limit);
        return convertToProducts(generateMockProductsForQuery(query, limit));
    }
    
    /**
     * Indexes a product with its embedding in OpenSearch.
     *
     * @param product The product to index
     */
    @Tracing
    public void indexProduct(FinancialProduct product) {
        logger.info("Mock indexing product: {}", product.getProductId());
    }
    
    /**
     * Generates mock products for testing.
     *
     * @param count The number of products to generate
     * @return A list of mock products
     */
    private List<FinancialProduct> generateMockProducts(int count) {
        List<FinancialProduct> products = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            FinancialProduct product = new FinancialProduct();
            product.setProductId("PROD-" + (1000 + i));
            product.setName("Mock Product " + (i + 1));
            product.setProductType(getRandomProductType());
            product.setCategory(getRandomCategory());
            product.setDescription("This is a mock product for testing purposes.");
            product.setInterestRate(new BigDecimal(String.format("%.2f", 1 + random.nextDouble() * 5)));
            product.setActive(true);
            
            products.add(product);
        }
        
        return products;
    }
    
    /**
     * Generates mock products based on a query for testing.
     *
     * @param query The query to base the products on
     * @param count The number of products to generate
     * @return A list of mock products
     */
    private List<FinancialProduct> generateMockProductsForQuery(String query, int count) {
        List<FinancialProduct> products = new ArrayList<>();
        
        // Extract keywords from the query to create more relevant mock products
        String lowercaseQuery = query.toLowerCase();
        String productType = determineProductTypeFromQuery(lowercaseQuery);
        String category = determineCategoryFromQuery(lowercaseQuery);
        
        for (int i = 0; i < count; i++) {
            FinancialProduct product = new FinancialProduct();
            product.setProductId("PROD-" + (1000 + i));
            product.setName(generateProductNameFromQuery(query, i));
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
    
    /**
     * Searches for products based on a query string.
     *
     * @param query The search query
     * @param limit The maximum number of results to return
     * @return List of financial products
     */
    @Override
    public List<FinancialProduct> searchProducts(String query, int limit) {
        logger.info("Mock searching for products with query: {}", query);
        return generateMockProductsForQuery(query, limit);
    }
    
    @Override
    public void close() {
        logger.info("Closing mock OpenSearchService");
    }

    private FinancialProduct generateMockProduct(String productId, String name, String category, String description, String assetClass) {
        FinancialProduct product = new FinancialProduct();
        product.setProductId(productId);
        product.setName(name);
        product.setCategory(category);
        product.setDescription(description);
        product.setAssetClass(assetClass);
        return product;
    }

    /**
     * Converts FinancialProduct objects to Product objects.
     *
     * @param financialProducts The list of FinancialProduct objects to convert
     * @return A list of Product objects
     */
    private List<Product> convertToProducts(List<FinancialProduct> financialProducts) {
        List<Product> products = new ArrayList<>();
        for (FinancialProduct fp : financialProducts) {
            Product product = new Product();
            product.setProductId(fp.getProductId());
            product.setName(fp.getName());
            product.setCategory(fp.getCategory());
            product.setDescription(fp.getDescription());
            // Set other properties as needed
            product.setIsActive(fp.isActive());
            
            products.add(product);
        }
        return products;
    }
} 