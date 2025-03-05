package com.enterprise.finance.personalization.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enterprise.finance.personalization.metrics.PersonalizationMetrics;
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
    private final PersonalizationMetrics metrics;
    
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
    
    // Personalization strategy constants
    public static final String STRATEGY_BASELINE = "BASELINE";
    public static final String STRATEGY_GOAL_PRIORITIZED = "GOAL_PRIORITIZED";
    public static final String STRATEGY_BEHAVIOR_DRIVEN = "BEHAVIOR_DRIVEN";
    public static final String STRATEGY_HYBRID = "HYBRID";
    
    /**
     * Constructor for NextBestActionService.
     *
     * @param bedrockService The service for generating embeddings
     * @param openSearchService The service for searching for similar products
     */
    public NextBestActionService(BedrockService bedrockService, MockOpenSearchService openSearchService) {
        this.bedrockService = bedrockService;
        this.openSearchService = openSearchService;
        this.metrics = PersonalizationMetrics.getInstance();
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
        return determineNextBestActions(userId, userPreferences, userBehaviors, financialContext, maxRecommendations, STRATEGY_BASELINE);
    }
    
    /**
     * Determines the next best actions for a user based on their preferences, behavior, and financial context,
     * using a specific personalization strategy.
     *
     * @param userId The ID of the user
     * @param userPreferences The user's preferences
     * @param userBehaviors The user's recent behaviors
     * @param financialContext The user's financial context
     * @param maxRecommendations The maximum number of recommendations to return
     * @param strategy The personalization strategy to use
     * @return A list of recommended actions
     */
    @Tracing
    public List<ActionRecommendation> determineNextBestActions(
            String userId,
            UserPreferences userPreferences,
            List<UserBehavior> userBehaviors,
            FinancialContext financialContext,
            int maxRecommendations,
            String strategy) {
        
        logger.info("Determining next best actions for user: {} using strategy: {}", userId, strategy);
        
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
            
            // Apply strategy-specific sorting and filtering
            recommendations = applyPersonalizationStrategy(recommendations, userPreferences, userBehaviors, financialContext, strategy);
            
            // Sort recommendations by confidence score and limit to max requested
            List<ActionRecommendation> finalRecommendations = recommendations.stream()
                    .sorted(Comparator.comparingDouble(ActionRecommendation::getConfidenceScore).reversed())
                    .limit(maxRecommendations)
                    .collect(Collectors.toList());
            
            // Record metrics for each recommendation
            for (ActionRecommendation recommendation : finalRecommendations) {
                metrics.recordRecommendationMade(userId, recommendation, strategy);
            }
            
            return finalRecommendations;
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
    
    /**
     * Applies a specific personalization strategy to the recommendations.
     *
     * @param recommendations The initial recommendations
     * @param userPreferences The user's preferences
     * @param userBehaviors The user's recent behaviors
     * @param financialContext The user's financial context
     * @param strategy The personalization strategy to apply
     * @return The adjusted recommendations
     */
    private List<ActionRecommendation> applyPersonalizationStrategy(
            List<ActionRecommendation> recommendations,
            UserPreferences userPreferences,
            List<UserBehavior> userBehaviors,
            FinancialContext financialContext,
            String strategy) {
        
        if (recommendations.isEmpty()) {
            return recommendations;
        }
        
        // Create a copy of the recommendations to avoid modifying the original list
        List<ActionRecommendation> adjustedRecommendations = new ArrayList<>(recommendations);
        
        switch (strategy) {
            case STRATEGY_GOAL_PRIORITIZED:
                // Prioritize recommendations that align with the user's financial goals
                if (financialContext != null && financialContext.getGoals() != null && !financialContext.getGoals().isEmpty()) {
                    // Extract goal types
                    List<String> goalTypes = financialContext.getGoals().stream()
                            .map(FinancialContext.FinancialGoal::getGoalType)
                            .collect(Collectors.toList());
                    
                    // Boost confidence scores for recommendations that align with goals
                    for (ActionRecommendation recommendation : adjustedRecommendations) {
                        if (recommendation.getActionType() == ActionType.INVESTMENT_OPPORTUNITY && 
                                goalTypes.contains("INVESTMENT")) {
                            recommendation.setConfidenceScore(recommendation.getConfidenceScore() * 1.5);
                        } else if (recommendation.getActionType() == ActionType.RETIREMENT_PLANNING && 
                                goalTypes.contains("RETIREMENT")) {
                            recommendation.setConfidenceScore(recommendation.getConfidenceScore() * 1.5);
                        } else if (recommendation.getActionType() == ActionType.SAVINGS_RECOMMENDATION && 
                                goalTypes.contains("SAVINGS")) {
                            recommendation.setConfidenceScore(recommendation.getConfidenceScore() * 1.5);
                        }
                    }
                }
                break;
                
            case STRATEGY_BEHAVIOR_DRIVEN:
                // Prioritize recommendations based on user behavior patterns
                if (userBehaviors != null && !userBehaviors.isEmpty()) {
                    // Count action types in user behaviors
                    Map<String, Integer> actionTypeCounts = new HashMap<>();
                    for (UserBehavior behavior : userBehaviors) {
                        String actionType = behavior.getActionType();
                        actionTypeCounts.put(actionType, actionTypeCounts.getOrDefault(actionType, 0) + 1);
                    }
                    
                    // Boost confidence scores for recommendations that align with frequent behaviors
                    for (ActionRecommendation recommendation : adjustedRecommendations) {
                        String actionTypeStr = recommendation.getActionType().toString();
                        if (actionTypeCounts.containsKey(actionTypeStr)) {
                            int count = actionTypeCounts.get(actionTypeStr);
                            double boost = 1.0 + (count / 10.0); // Boost based on frequency
                            recommendation.setConfidenceScore(recommendation.getConfidenceScore() * boost);
                        }
                    }
                }
                break;
                
            case STRATEGY_HYBRID:
                // Apply both goal and behavior strategies with balanced weights
                adjustedRecommendations = applyPersonalizationStrategy(
                        recommendations, userPreferences, userBehaviors, financialContext, STRATEGY_GOAL_PRIORITIZED);
                adjustedRecommendations = applyPersonalizationStrategy(
                        adjustedRecommendations, userPreferences, userBehaviors, financialContext, STRATEGY_BEHAVIOR_DRIVEN);
                
                // Normalize confidence scores
                double maxScore = adjustedRecommendations.stream()
                        .mapToDouble(ActionRecommendation::getConfidenceScore)
                        .max()
                        .orElse(1.0);
                
                for (ActionRecommendation recommendation : adjustedRecommendations) {
                    recommendation.setConfidenceScore(recommendation.getConfidenceScore() / maxScore);
                }
                break;
                
            case STRATEGY_BASELINE:
            default:
                // No adjustments for baseline strategy
                break;
        }
        
        return adjustedRecommendations;
    }
    
    /**
     * Records that a user has accepted a recommendation.
     *
     * @param userId The ID of the user
     * @param recommendation The recommendation that was accepted
     * @param strategy The personalization strategy that was used
     */
    public void recordRecommendationAccepted(String userId, ActionRecommendation recommendation, String strategy) {
        metrics.recordRecommendationAccepted(userId, recommendation, strategy);
        logger.info("User {} accepted recommendation: {}", userId, recommendation.getActionType());
    }
    
    /**
     * Records the financial impact of a recommendation.
     *
     * @param userId The ID of the user
     * @param recommendation The recommendation
     * @param financialImpactDollars The financial impact in dollars
     */
    public void recordFinancialImpact(String userId, ActionRecommendation recommendation, BigDecimal financialImpactDollars) {
        // Convert dollars to cents for storage
        long impactCents = financialImpactDollars.multiply(new BigDecimal(100)).longValue();
        metrics.recordFinancialImpact(userId, recommendation, impactCents);
        logger.info("Recorded financial impact of ${} for user {} recommendation: {}", 
                  financialImpactDollars, userId, recommendation.getActionType());
    }
    
    /**
     * Records user satisfaction with a recommendation.
     *
     * @param userId The ID of the user
     * @param recommendation The recommendation
     * @param satisfactionScore The satisfaction score (1-5)
     */
    public void recordUserSatisfaction(String userId, ActionRecommendation recommendation, int satisfactionScore) {
        metrics.recordUserSatisfaction(userId, recommendation, satisfactionScore);
        logger.info("User {} rated recommendation {} with score: {}", 
                  userId, recommendation.getActionType(), satisfactionScore);
    }
    
    /**
     * Generates a metrics report for personalization effectiveness.
     *
     * @return A string containing the metrics report
     */
    public String generateMetricsReport() {
        return metrics.generateMetricsReport();
    }
    
    @Override
    public void close() {
        logger.info("Closing NextBestActionService");
    }
} 