package com.enterprise.finance.personalization.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enterprise.finance.personalization.model.FinancialContext;
import com.enterprise.finance.personalization.model.FinancialProduct;
import com.enterprise.finance.personalization.model.UserBehavior;
import com.enterprise.finance.personalization.model.UserPreferences;

import software.amazon.lambda.powertools.tracing.Tracing;

/**
 * Service for determining the next best actions for users based on their profile and behavior.
 * Uses embeddings to match user profiles with appropriate financial actions.
 */
public class NextBestActionService implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(NextBestActionService.class);
    
    private final BedrockService bedrockService;
    private final MockOpenSearchService openSearchService;
    
    /**
     * Enum representing different types of financial actions that can be recommended.
     */
    public enum ActionType {
        INVESTMENT_OPPORTUNITY,
        SAVINGS_RECOMMENDATION,
        DEBT_MANAGEMENT,
        RETIREMENT_PLANNING,
        INSURANCE_RECOMMENDATION,
        TAX_OPTIMIZATION,
        PORTFOLIO_REBALANCING,
        BEHAVIORAL_COACHING,
        ASSET_LOCATION_OPTIMIZATION,
        WITHDRAWAL_STRATEGY,
        ESTATE_PLANNING,
        LIFE_EVENT_PLANNING
    }
    
    /**
     * Class representing a recommended action for a user.
     */
    public static class ActionRecommendation {
        private ActionType actionType;
        private String description;
        private double confidenceScore;
        private String context;
        private List<FinancialProduct> relatedProducts;

        // Constructor
        public ActionRecommendation() {
            this.relatedProducts = new ArrayList<>();
        }
        
        // Constructor with parameters
        public ActionRecommendation(ActionType actionType, String description, double confidenceScore, String context, List<FinancialProduct> relatedProducts) {
            this.actionType = actionType;
            this.description = description;
            this.confidenceScore = confidenceScore;
            this.context = context;
            this.relatedProducts = relatedProducts != null ? relatedProducts : new ArrayList<>();
        }

        // Getters and setters
        public ActionType getActionType() { return actionType; }
        public void setActionType(ActionType actionType) { this.actionType = actionType; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }

        public List<FinancialProduct> getRelatedProducts() { return relatedProducts; }
        public void setRelatedProducts(List<FinancialProduct> relatedProducts) { this.relatedProducts = relatedProducts; }
    }
    
    /**
     * Constructor for NextBestActionService.
     *
     * @param bedrockService The service for generating embeddings
     * @param openSearchService The service for searching for similar products
     */
    public NextBestActionService(BedrockService bedrockService, MockOpenSearchService openSearchService) {
        this.bedrockService = bedrockService;
        this.openSearchService = openSearchService;
        logger.info("NextBestActionService initialized");
    }
    
    /**
     * Determines the next best actions for a user based on their preferences, behavior, and financial context.
     *
     * @param userId The ID of the user
     * @param userPreferences The user's preferences
     * @param userBehaviors The user's recent behaviors
     * @param financialContext The user's financial context
     * @param maxRecommendations The maximum number of recommendations to return
     * @return A list of recommended actions
     */
    @Tracing
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
            float[] userProfileEmbedding = bedrockService.generateEmbedding(userProfileText);
            
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
    
    /**
     * Overloaded method that uses the user ID from the preferences and a default max of 5 recommendations.
     *
     * @param userPreferences The user's preferences
     * @param userBehaviors The user's recent behaviors
     * @param financialContext The user's financial context
     * @return A list of recommended actions
     */
    @Tracing
    public List<ActionRecommendation> determineNextBestActions(
            UserPreferences userPreferences,
            List<UserBehavior> userBehaviors,
            FinancialContext financialContext) {
        
        String userId = userPreferences != null ? userPreferences.getUserId() : "unknown";
        return determineNextBestActions(userId, userPreferences, userBehaviors, financialContext, 5);
    }
    
    /**
     * Builds a text representation of the user profile for embedding generation.
     *
     * @param preferences The user's preferences
     * @param behaviors The user's recent behaviors
     * @param context The user's financial context
     * @return A text representation of the user profile
     */
    private String buildUserProfileText(UserPreferences preferences, List<UserBehavior> behaviors, FinancialContext context) {
        StringBuilder sb = new StringBuilder();
        
        // Add user preferences
        sb.append("User Preferences:\n");
        sb.append("Risk Tolerance: ").append(preferences.getRiskTolerance()).append("\n");
        sb.append("Investment Horizon: ").append(preferences.getInvestmentHorizon()).append("\n");
        
        if (preferences.getPreferredCategories() != null && !preferences.getPreferredCategories().isEmpty()) {
            sb.append("Preferred Categories: ").append(String.join(", ", preferences.getPreferredCategories())).append("\n");
        }
        
        // Add recent user behaviors
        sb.append("\nRecent User Behaviors:\n");
        if (behaviors != null && !behaviors.isEmpty()) {
            // Sort behaviors by timestamp, most recent first
            List<UserBehavior> sortedBehaviors = behaviors.stream()
                    .sorted(Comparator.comparing(UserBehavior::getTimestamp).reversed())
                    .limit(10) // Only include the 10 most recent behaviors
                    .collect(Collectors.toList());
            
            for (UserBehavior behavior : sortedBehaviors) {
                sb.append("- ").append(behavior.getActionType())
                  .append(" ").append(behavior.getResourceType())
                  .append(" (").append(behavior.getTimestamp()).append(")\n");
            }
        } else {
            sb.append("No recent behaviors recorded.\n");
        }
        
        // Add financial context
        sb.append("\nFinancial Context:\n");
        if (context != null) {
            sb.append("Monthly Income: ").append(context.getMonthlyIncome()).append("\n");
            sb.append("Total Savings: ").append(context.getTotalSavings()).append("\n");
            sb.append("Total Debt: ").append(context.getTotalDebt()).append("\n");
            sb.append("Credit Score: ").append(context.getCreditScore()).append("\n");
            sb.append("Monthly Expenses: ").append(context.getMonthlyExpenses()).append("\n");
            sb.append("Disposable Income: ").append(context.getDisposableIncome()).append("\n");
            sb.append("Savings Rate: ").append(context.getSavingsRate()).append("\n");
            sb.append("Debt-to-Income Ratio: ").append(context.getDebtToIncomeRatio()).append("\n");
            
            // Add accounts
            if (context.getAccounts() != null && !context.getAccounts().isEmpty()) {
                sb.append("\nAccounts:\n");
                for (FinancialContext.Account account : context.getAccounts()) {
                    sb.append("- ").append(account.getAccountName())
                      .append(" (").append(account.getAccountType())
                      .append("): ").append(account.getBalance()).append("\n");
                }
            }
            
            // Add goals
            if (context.getGoals() != null && !context.getGoals().isEmpty()) {
                sb.append("\nFinancial Goals:\n");
                for (FinancialContext.FinancialGoal goal : context.getGoals()) {
                    sb.append("- ").append(goal.getDescription())
                      .append(" (").append(goal.getGoalType())
                      .append("): ").append(goal.getCurrentAmount())
                      .append(" / ").append(goal.getTargetAmount())
                      .append(" by ").append(goal.getTargetDate()).append("\n");
                }
            }
        } else {
            sb.append("No financial context available.\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Generates savings recommendations based on the user's financial context.
     *
     * @param context The user's financial context
     * @return A list of savings recommendations
     */
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
    
    /**
     * Generates debt management recommendations based on the user's financial context.
     *
     * @param context The user's financial context
     * @return A list of debt management recommendations
     */
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
    
    /**
     * Generates investment recommendations based on the user's preferences and financial context.
     *
     * @param preferences The user's preferences
     * @param context The user's financial context
     * @return A list of investment recommendations
     */
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
    
    /**
     * Generates credit improvement recommendations based on the user's financial context.
     *
     * @param context The user's financial context
     * @return A list of credit improvement recommendations
     */
    private List<ActionRecommendation> generateCreditImprovementRecommendations(FinancialContext context) {
        List<ActionRecommendation> recommendations = new ArrayList<>();
        
        if (context == null || context.getCreditScore() == null) {
            return recommendations;
        }
        
        // Check if credit score is below 700
        if (context.getCreditScore().compareTo(new BigDecimal("700")) < 0) {
            double confidenceScore = 0.9 - (context.getCreditScore().doubleValue() / 1000.0); // Higher confidence for lower scores
            
            recommendations.add(new ActionRecommendation(
                ActionType.SAVINGS_RECOMMENDATION,
                "Improve your credit score to qualify for better financial products.",
                confidenceScore,
                "Your current credit score is " + context.getCreditScore() + ", " +
                "which is below the 'good' threshold of 700.",
                new ArrayList<>()
            ));
        }
        
        return recommendations;
    }
    
    /**
     * Enriches recommendations with related financial products using embeddings.
     *
     * @param recommendations The list of recommendations to enrich
     * @param userProfileEmbedding The user profile embedding
     * @throws IOException If an error occurs during the search
     */
    private void enrichRecommendationsWithProducts(List<ActionRecommendation> recommendations, float[] userProfileEmbedding) throws IOException {
        for (ActionRecommendation recommendation : recommendations) {
            try {
                // Create a search query combining the recommendation type and user profile
                String searchQuery = recommendation.getActionType().toString() + " " + recommendation.getDescription();
                
                // Search for relevant products
                List<FinancialProduct> relevantProducts = openSearchService.searchProducts(searchQuery, 3);
                
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
    
    @Override
    public void close() {
        logger.info("Closing NextBestActionService");
    }
} 