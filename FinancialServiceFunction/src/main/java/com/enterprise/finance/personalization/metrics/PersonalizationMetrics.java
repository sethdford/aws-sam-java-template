package com.enterprise.finance.personalization.metrics;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enterprise.finance.personalization.service.NextBestActionService.ActionRecommendation;
import com.enterprise.finance.personalization.service.NextBestActionService.ActionType;

import software.amazon.lambda.powertools.metrics.Metrics;

/**
 * Tracks metrics related to personalization effectiveness based on industry research.
 * This class implements the metrics mentioned in the Vanguard "Value of Personalized Advice" research.
 */
public class PersonalizationMetrics {
    private static final Logger logger = LoggerFactory.getLogger(PersonalizationMetrics.class);
    
    // Singleton instance
    private static PersonalizationMetrics instance;
    
    // Metrics counters
    private final Map<String, AtomicInteger> recommendationCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> acceptedRecommendationCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> financialImpactSum = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> userSatisfactionScores = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> userSatisfactionCounts = new ConcurrentHashMap<>();
    
    // A/B testing strategy tracking
    private final Map<String, Map<String, AtomicInteger>> strategyPerformance = new ConcurrentHashMap<>();
    
    /**
     * Private constructor for singleton pattern
     */
    private PersonalizationMetrics() {
        // Initialize metrics for each action type
        for (ActionType actionType : ActionType.values()) {
            String actionName = actionType.name();
            recommendationCounts.put(actionName, new AtomicInteger(0));
            acceptedRecommendationCounts.put(actionName, new AtomicInteger(0));
            financialImpactSum.put(actionName, new AtomicLong(0));
            userSatisfactionScores.put(actionName, new AtomicInteger(0));
            userSatisfactionCounts.put(actionName, new AtomicInteger(0));
        }
        
        // Initialize strategy performance tracking
        strategyPerformance.put("BASELINE", new HashMap<>());
        strategyPerformance.put("GOAL_PRIORITIZED", new HashMap<>());
        strategyPerformance.put("BEHAVIOR_DRIVEN", new HashMap<>());
        strategyPerformance.put("HYBRID", new HashMap<>());
        
        for (Map<String, AtomicInteger> strategyMetrics : strategyPerformance.values()) {
            for (ActionType actionType : ActionType.values()) {
                strategyMetrics.put(actionType.name(), new AtomicInteger(0));
            }
        }
        
        logger.info("PersonalizationMetrics initialized");
    }
    
    /**
     * Get the singleton instance
     * 
     * @return The PersonalizationMetrics instance
     */
    public static synchronized PersonalizationMetrics getInstance() {
        if (instance == null) {
            instance = new PersonalizationMetrics();
        }
        return instance;
    }
    
    /**
     * Record that a recommendation was made
     * 
     * @param userId The user ID
     * @param recommendation The recommendation that was made
     * @param strategy The personalization strategy used
     */
    @Metrics(namespace = "Personalization")
    public void recordRecommendationMade(String userId, ActionRecommendation recommendation, String strategy) {
        if (recommendation == null || recommendation.getActionType() == null) {
            return;
        }
        
        String actionName = recommendation.getActionType().name();
        recommendationCounts.get(actionName).incrementAndGet();
        
        // Record for A/B testing if strategy is provided
        if (strategy != null && strategyPerformance.containsKey(strategy)) {
            Map<String, AtomicInteger> metrics = strategyPerformance.get(strategy);
            if (metrics.containsKey(actionName)) {
                metrics.get(actionName).incrementAndGet();
            }
        }
        
        logger.debug("Recorded recommendation made: userId={}, actionType={}, strategy={}", 
                   userId, actionName, strategy);
    }
    
    /**
     * Record that a recommendation was accepted by the user
     * 
     * @param userId The user ID
     * @param recommendation The recommendation that was accepted
     * @param strategy The personalization strategy used
     */
    @Metrics(namespace = "Personalization")
    public void recordRecommendationAccepted(String userId, ActionRecommendation recommendation, String strategy) {
        if (recommendation == null || recommendation.getActionType() == null) {
            return;
        }
        
        String actionName = recommendation.getActionType().name();
        acceptedRecommendationCounts.get(actionName).incrementAndGet();
        
        logger.debug("Recorded recommendation accepted: userId={}, actionType={}, strategy={}", 
                   userId, actionName, strategy);
    }
    
    /**
     * Record the financial impact of a recommendation
     * 
     * @param userId The user ID
     * @param recommendation The recommendation
     * @param financialImpact The financial impact in cents (to avoid floating point issues)
     */
    @Metrics(namespace = "Personalization")
    public void recordFinancialImpact(String userId, ActionRecommendation recommendation, long financialImpact) {
        if (recommendation == null || recommendation.getActionType() == null) {
            return;
        }
        
        String actionName = recommendation.getActionType().name();
        financialImpactSum.get(actionName).addAndGet(financialImpact);
        
        logger.debug("Recorded financial impact: userId={}, actionType={}, impact={}", 
                   userId, actionName, financialImpact);
    }
    
    /**
     * Record user satisfaction with a recommendation
     * 
     * @param userId The user ID
     * @param recommendation The recommendation
     * @param satisfactionScore The satisfaction score (1-5)
     */
    @Metrics(namespace = "Personalization")
    public void recordUserSatisfaction(String userId, ActionRecommendation recommendation, int satisfactionScore) {
        if (recommendation == null || recommendation.getActionType() == null || 
            satisfactionScore < 1 || satisfactionScore > 5) {
            return;
        }
        
        String actionName = recommendation.getActionType().name();
        userSatisfactionScores.get(actionName).addAndGet(satisfactionScore);
        userSatisfactionCounts.get(actionName).incrementAndGet();
        
        logger.debug("Recorded user satisfaction: userId={}, actionType={}, score={}", 
                   userId, actionName, satisfactionScore);
    }
    
    /**
     * Get the acceptance rate for a specific action type
     * 
     * @param actionType The action type
     * @return The acceptance rate as a decimal (0.0 to 1.0)
     */
    public double getAcceptanceRate(ActionType actionType) {
        if (actionType == null) {
            return 0.0;
        }
        
        String actionName = actionType.name();
        int recommendations = recommendationCounts.get(actionName).get();
        int acceptances = acceptedRecommendationCounts.get(actionName).get();
        
        return recommendations > 0 ? (double) acceptances / recommendations : 0.0;
    }
    
    /**
     * Get the average financial impact for a specific action type
     * 
     * @param actionType The action type
     * @return The average financial impact in dollars
     */
    public BigDecimal getAverageFinancialImpact(ActionType actionType) {
        if (actionType == null) {
            return BigDecimal.ZERO;
        }
        
        String actionName = actionType.name();
        long totalImpactCents = financialImpactSum.get(actionName).get();
        int acceptances = acceptedRecommendationCounts.get(actionName).get();
        
        if (acceptances > 0) {
            // Convert cents to dollars with 2 decimal places
            return new BigDecimal(totalImpactCents)
                    .divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP)
                    .divide(new BigDecimal(acceptances), 2, BigDecimal.ROUND_HALF_UP);
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Get the average satisfaction score for a specific action type
     * 
     * @param actionType The action type
     * @return The average satisfaction score (1.0 to 5.0)
     */
    public double getAverageSatisfactionScore(ActionType actionType) {
        if (actionType == null) {
            return 0.0;
        }
        
        String actionName = actionType.name();
        int totalScore = userSatisfactionScores.get(actionName).get();
        int count = userSatisfactionCounts.get(actionName).get();
        
        return count > 0 ? (double) totalScore / count : 0.0;
    }
    
    /**
     * Get the performance of different personalization strategies
     * 
     * @return A map of strategy names to their acceptance rates
     */
    public Map<String, Double> getStrategyPerformance() {
        Map<String, Double> performance = new HashMap<>();
        
        for (Map.Entry<String, Map<String, AtomicInteger>> entry : strategyPerformance.entrySet()) {
            String strategy = entry.getKey();
            Map<String, AtomicInteger> metrics = entry.getValue();
            
            int totalRecommendations = 0;
            int totalAcceptances = 0;
            
            for (ActionType actionType : ActionType.values()) {
                String actionName = actionType.name();
                if (metrics.containsKey(actionName)) {
                    totalRecommendations += metrics.get(actionName).get();
                }
                if (acceptedRecommendationCounts.containsKey(actionName)) {
                    totalAcceptances += acceptedRecommendationCounts.get(actionName).get();
                }
            }
            
            double acceptanceRate = totalRecommendations > 0 ? 
                    (double) totalAcceptances / totalRecommendations : 0.0;
            
            performance.put(strategy, acceptanceRate);
        }
        
        return performance;
    }
    
    /**
     * Generate a metrics report with key personalization effectiveness indicators
     * 
     * @return A string containing the metrics report
     */
    public String generateMetricsReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Personalization Effectiveness Metrics ===\n");
        report.append("Generated at: ").append(Instant.now()).append("\n\n");
        
        report.append("Recommendation Acceptance Rates:\n");
        for (ActionType actionType : ActionType.values()) {
            String actionName = actionType.name();
            int recommendations = recommendationCounts.get(actionName).get();
            int acceptances = acceptedRecommendationCounts.get(actionName).get();
            double rate = recommendations > 0 ? (double) acceptances / recommendations : 0.0;
            
            report.append(String.format("- %s: %.2f%% (%d/%d)\n", 
                    actionName, rate * 100, acceptances, recommendations));
        }
        
        report.append("\nAverage Financial Impact:\n");
        for (ActionType actionType : ActionType.values()) {
            BigDecimal impact = getAverageFinancialImpact(actionType);
            report.append(String.format("- %s: $%s\n", actionType.name(), impact.toString()));
        }
        
        report.append("\nAverage User Satisfaction (1-5):\n");
        for (ActionType actionType : ActionType.values()) {
            String actionName = actionType.name();
            int totalScore = userSatisfactionScores.get(actionName).get();
            int count = userSatisfactionCounts.get(actionName).get();
            double avgScore = count > 0 ? (double) totalScore / count : 0.0;
            
            report.append(String.format("- %s: %.2f (%d ratings)\n", 
                    actionName, avgScore, count));
        }
        
        report.append("\nA/B Testing Strategy Performance:\n");
        Map<String, Double> strategyPerf = getStrategyPerformance();
        for (Map.Entry<String, Double> entry : strategyPerf.entrySet()) {
            report.append(String.format("- %s: %.2f%%\n", 
                    entry.getKey(), entry.getValue() * 100));
        }
        
        return report.toString();
    }
} 