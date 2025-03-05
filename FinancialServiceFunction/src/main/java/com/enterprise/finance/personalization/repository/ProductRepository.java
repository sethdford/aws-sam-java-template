package com.enterprise.finance.personalization.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.enterprise.finance.personalization.model.Product;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Repository class for Product entity operations in DynamoDB.
 */
public class ProductRepository {
    private static final Logger logger = LogManager.getLogger(ProductRepository.class);
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Product> productTable;
    private final String tableName;

    public ProductRepository() {
        String tableName = System.getenv("PRODUCT_CATALOG_TABLE");
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalStateException("PRODUCT_CATALOG_TABLE environment variable is not set");
        }
        this.tableName = tableName;

        String region = System.getenv("AWS_REGION");
        if (region == null || region.isEmpty()) {
            region = "us-east-1"; // Default region
        }

        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(region))
                .build();

        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        this.productTable = enhancedClient.table(tableName, TableSchema.fromBean(Product.class));
        logger.info("Initialized ProductRepository with table: {}", tableName);
    }

    /**
     * Get a product by its ID.
     *
     * @param productId The product ID
     * @return Optional containing the product if found
     */
    public Optional<Product> getProductById(String productId) {
        try {
            Product product = productTable.getItem(r -> r.key(k -> k.partitionValue(productId)));
            return Optional.ofNullable(product);
        } catch (Exception e) {
            logger.error("Error retrieving product with ID {}: {}", productId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get all products.
     *
     * @return List of all products
     */
    public List<Product> getAllProducts() {
        try {
            PageIterable<Product> products = productTable.scan();
            return products.items().stream().collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving all products: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get products by category.
     *
     * @param category The product category
     * @return List of products in the specified category
     */
    public List<Product> getProductsByCategory(String category) {
        try {
            // In a real implementation, you would use a GSI for category
            // For simplicity, we're scanning and filtering here
            PageIterable<Product> products = productTable.scan();
            return products.items().stream()
                    .filter(p -> category.equals(p.getCategory()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving products by category {}: {}", category, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Save a product.
     *
     * @param product The product to save
     * @return The saved product
     */
    public Product saveProduct(Product product) {
        try {
            productTable.putItem(product);
            logger.info("Saved product with ID: {}", product.getProductId());
            return product;
        } catch (Exception e) {
            logger.error("Error saving product with ID {}: {}", product.getProductId(), e.getMessage());
            throw new RuntimeException("Failed to save product", e);
        }
    }

    /**
     * Delete a product.
     *
     * @param productId The ID of the product to delete
     */
    public void deleteProduct(String productId) {
        try {
            productTable.deleteItem(r -> r.key(k -> k.partitionValue(productId)));
            logger.info("Deleted product with ID: {}", productId);
        } catch (Exception e) {
            logger.error("Error deleting product with ID {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to delete product", e);
        }
    }
} 