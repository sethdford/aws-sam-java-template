package com.enterprise.finance.personalization;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.enterprise.finance.personalization.model.FinancialContext;
import com.enterprise.finance.personalization.model.UserBehavior;
import com.enterprise.finance.personalization.model.UserPreferences;
import com.enterprise.finance.personalization.service.BedrockService;
import com.enterprise.finance.personalization.service.MockOpenSearchService;
import com.enterprise.finance.personalization.service.NextBestActionService;
import com.enterprise.finance.personalization.service.NextBestActionService.ActionRecommendation;

/**
 * Example demonstrating how to use the NextBestActionService with metrics tracking
 * to measure personalization effectiveness based on industry research.
 */
public class PersonalizationMetricsExample {

    public static void main(String[] args) {
        System.out.println("Starting Personalization Metrics Example...");
        
        // Initialize services
        BedrockService bedrockService = new BedrockService();
        MockOpenSearchService openSearchService = new MockOpenSearchService();
        NextBestActionService nextBestActionService = new NextBestActionService(bedrockService, openSearchService);
        
        // Run A/B testing with different personalization strategies
        runABTest(nextBestActionService, 100); // Simulate 100 users
        
        // Generate and print metrics report
        String metricsReport = nextBestActionService.generateMetricsReport();
        System.out.println("\n" + metricsReport);
        
        System.out.println("Personalization Metrics Example completed.");
    }
    
    /**
     * Runs an A/B test with different personalization strategies.
     * 
     * @param service The NextBestActionService
     * @param userCount The number of simulated users
     */
    private static void runABTest(NextBestActionService service, int userCount) {
        System.out.println("Running A/B test with " + userCount + " simulated users...");
        
        Random random = new Random();
        String[] strategies = {
            NextBestActionService.STRATEGY_BASELINE,
            NextBestActionService.STRATEGY_GOAL_PRIORITIZED,
            NextBestActionService.STRATEGY_BEHAVIOR_DRIVEN,
            NextBestActionService.STRATEGY_HYBRID
        };
        
        for (int i = 0; i < userCount; i++) {
            // Generate a random user ID
            String userId = "user-" + UUID.randomUUID().toString().substring(0, 8);
            
            // Create random user profile
            UserPreferences preferences = createRandomUserPreferences(userId, random);
            List<UserBehavior> behaviors = createRandomUserBehaviors(userId, random, 5);
            FinancialContext context = createRandomFinancialContext(userId, random);
            
            // Select a random strategy for this user
            String strategy = strategies[random.nextInt(strategies.length)];
            
            // Get recommendations using the selected strategy
            List<ActionRecommendation> recommendations = service.determineNextBestActions(
                    userId, preferences, behaviors, context, 3, strategy);
            
            // Simulate user interactions with recommendations
            simulateUserInteractions(service, userId, recommendations, strategy, random);
            
            if ((i + 1) % 10 == 0) {
                System.out.println("Processed " + (i + 1) + " users...");
            }
        }
        
        System.out.println("A/B test completed.");
    }
    
    /**
     * Simulates user interactions with recommendations.
     * 
     * @param service The NextBestActionService
     * @param userId The user ID
     * @param recommendations The recommendations
     * @param strategy The personalization strategy used
     * @param random Random number generator
     */
    private static void simulateUserInteractions(
            NextBestActionService service, 
            String userId, 
            List<ActionRecommendation> recommendations,
            String strategy,
            Random random) {
        
        for (ActionRecommendation recommendation : recommendations) {
            // Simulate whether the user accepts the recommendation (higher confidence = higher chance)
            double acceptanceProbability = recommendation.getConfidenceScore() * 0.8; // Base probability on confidence
            
            // Adjust acceptance probability based on strategy (simulating that some strategies work better)
            if (strategy.equals(NextBestActionService.STRATEGY_HYBRID)) {
                acceptanceProbability *= 1.2; // Hybrid strategy performs better in our simulation
            } else if (strategy.equals(NextBestActionService.STRATEGY_GOAL_PRIORITIZED)) {
                acceptanceProbability *= 1.1; // Goal-prioritized strategy also performs well
            }
            
            // Cap probability at 95%
            acceptanceProbability = Math.min(acceptanceProbability, 0.95);
            
            if (random.nextDouble() < acceptanceProbability) {
                // User accepted the recommendation
                service.recordRecommendationAccepted(userId, recommendation, strategy);
                
                // Simulate financial impact (between $100 and $5000)
                BigDecimal financialImpact = new BigDecimal(random.nextInt(4900) + 100);
                service.recordFinancialImpact(userId, recommendation, financialImpact);
                
                // Simulate user satisfaction (1-5 scale, weighted toward higher scores for accepted recommendations)
                int satisfactionScore = random.nextInt(3) + 3; // 3-5 range for accepted recommendations
                service.recordUserSatisfaction(userId, recommendation, satisfactionScore);
            } else {
                // User did not accept the recommendation, but may still provide satisfaction feedback
                if (random.nextDouble() < 0.3) { // 30% chance of providing feedback even when not accepting
                    int satisfactionScore = random.nextInt(3) + 1; // 1-3 range for non-accepted recommendations
                    service.recordUserSatisfaction(userId, recommendation, satisfactionScore);
                }
            }
        }
    }
    
    /**
     * Creates random user preferences.
     * 
     * @param userId The user ID
     * @param random Random number generator
     * @return Random user preferences
     */
    private static UserPreferences createRandomUserPreferences(String userId, Random random) {
        UserPreferences preferences = new UserPreferences();
        preferences.setUserId(userId);
        
        // Set random risk tolerance (1-10)
        preferences.setRiskTolerance(random.nextInt(10) + 1);
        
        // Set random investment horizon (1-30 years)
        preferences.setInvestmentHorizon(String.valueOf(random.nextInt(30) + 1));
        
        // Set random preferred categories
        String[] possibleCategories = {"STOCKS", "BONDS", "MUTUAL_FUNDS", "ETFS", "REAL_ESTATE", "CRYPTO"};
        List<String> preferredCategories = new ArrayList<>();
        
        int categoryCount = random.nextInt(3) + 1; // 1-3 categories
        for (int i = 0; i < categoryCount; i++) {
            String category = possibleCategories[random.nextInt(possibleCategories.length)];
            if (!preferredCategories.contains(category)) {
                preferredCategories.add(category);
            }
        }
        
        preferences.setPreferredCategories(preferredCategories);
        
        return preferences;
    }
    
    /**
     * Creates random user behaviors.
     * 
     * @param userId The user ID
     * @param random Random number generator
     * @param count The number of behaviors to create
     * @return Random user behaviors
     */
    private static List<UserBehavior> createRandomUserBehaviors(String userId, Random random, int count) {
        List<UserBehavior> behaviors = new ArrayList<>();
        
        String[] actionTypes = {"VIEW", "SEARCH", "CLICK", "SAVE", "PURCHASE"};
        String[] resourceTypes = {"PRODUCT", "ARTICLE", "CALCULATOR", "VIDEO", "RECOMMENDATION"};
        
        // Generate behaviors over the last 30 days
        long now = Instant.now().toEpochMilli();
        long thirtyDaysAgo = Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS).toEpochMilli();
        
        for (int i = 0; i < count; i++) {
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setActionType(actionTypes[random.nextInt(actionTypes.length)]);
            behavior.setResourceType(resourceTypes[random.nextInt(resourceTypes.length)]);
            
            // Random timestamp in the last 30 days
            long timestamp = thirtyDaysAgo + (long)(random.nextDouble() * (now - thirtyDaysAgo));
            behavior.setTimestamp(Instant.ofEpochMilli(timestamp));
            
            // Random resource ID
            behavior.setResourceId("resource-" + UUID.randomUUID().toString().substring(0, 8));
            
            behaviors.add(behavior);
        }
        
        return behaviors;
    }
    
    /**
     * Creates a random financial context.
     * 
     * @param userId The user ID
     * @param random Random number generator
     * @return Random financial context
     */
    private static FinancialContext createRandomFinancialContext(String userId, Random random) {
        FinancialContext context = new FinancialContext();
        context.setUserId(userId);
        
        // Set random financial values
        context.setMonthlyIncome(new BigDecimal(3000 + random.nextInt(12000)));
        context.setMonthlyExpenses(context.getMonthlyIncome().multiply(new BigDecimal(0.4 + random.nextDouble() * 0.4)));
        context.setTotalSavings(context.getMonthlyIncome().multiply(new BigDecimal(random.nextInt(24))));
        context.setTotalDebt(context.getMonthlyIncome().multiply(new BigDecimal(random.nextInt(36))));
        context.setCreditScore(new BigDecimal(550 + random.nextInt(300)));
        
        // Calculate derived values
        context.setDisposableIncome(context.getMonthlyIncome().subtract(context.getMonthlyExpenses()));
        
        BigDecimal savingsRate = context.getDisposableIncome().divide(context.getMonthlyIncome(), 2, BigDecimal.ROUND_HALF_UP);
        context.setSavingsRate(savingsRate);
        
        BigDecimal debtToIncomeRatio = context.getTotalDebt().divide(context.getMonthlyIncome().multiply(new BigDecimal(12)), 2, BigDecimal.ROUND_HALF_UP);
        context.setDebtToIncomeRatio(debtToIncomeRatio);
        
        // Add random accounts
        List<FinancialContext.Account> accounts = new ArrayList<>();
        String[] accountTypes = {"CHECKING", "SAVINGS", "INVESTMENT", "RETIREMENT", "CREDIT_CARD", "LOAN"};
        
        int accountCount = random.nextInt(4) + 1; // 1-4 accounts
        for (int i = 0; i < accountCount; i++) {
            FinancialContext.Account account = new FinancialContext.Account();
            account.setAccountId("account-" + UUID.randomUUID().toString().substring(0, 8));
            account.setAccountName("Account " + (i + 1));
            account.setAccountType(accountTypes[random.nextInt(accountTypes.length)]);
            
            // Set balance based on account type
            if (account.getAccountType().equals("CREDIT_CARD") || account.getAccountType().equals("LOAN")) {
                account.setBalance(new BigDecimal(-1000 - random.nextInt(20000)));
            } else {
                account.setBalance(new BigDecimal(100 + random.nextInt(50000)));
            }
            
            accounts.add(account);
        }
        
        context.setAccounts(accounts);
        
        // Add random financial goals
        List<FinancialContext.FinancialGoal> goals = new ArrayList<>();
        String[] goalTypes = {"RETIREMENT", "SAVINGS", "INVESTMENT", "DEBT_PAYOFF", "MAJOR_PURCHASE"};
        String[] descriptions = {
            "Retirement savings", 
            "Emergency fund", 
            "Down payment for house", 
            "Pay off student loans", 
            "Save for vacation",
            "College fund"
        };
        
        int goalCount = random.nextInt(3) + 1; // 1-3 goals
        for (int i = 0; i < goalCount; i++) {
            FinancialContext.FinancialGoal goal = new FinancialContext.FinancialGoal();
            goal.setGoalId("goal-" + UUID.randomUUID().toString().substring(0, 8));
            goal.setGoalType(goalTypes[random.nextInt(goalTypes.length)]);
            goal.setDescription(descriptions[random.nextInt(descriptions.length)]);
            
            // Set target amount based on goal type
            if (goal.getGoalType().equals("RETIREMENT")) {
                goal.setTargetAmount(new BigDecimal(500000 + random.nextInt(1500000)));
            } else if (goal.getGoalType().equals("MAJOR_PURCHASE")) {
                goal.setTargetAmount(new BigDecimal(10000 + random.nextInt(90000)));
            } else {
                goal.setTargetAmount(new BigDecimal(1000 + random.nextInt(49000)));
            }
            
            // Set current amount (0-90% of target)
            double progressPercentage = random.nextDouble() * 0.9;
            goal.setCurrentAmount(goal.getTargetAmount().multiply(new BigDecimal(progressPercentage)).setScale(2, BigDecimal.ROUND_HALF_UP));
            
            // Fix: Convert LocalDate to Instant for goal dates
            LocalDate now = LocalDate.now();
            Instant createdDate = now.minusDays(random.nextInt(365)).atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant targetDate = now.plusYears(1 + random.nextInt(10)).atStartOfDay(ZoneId.systemDefault()).toInstant();
            
            goal.setCreatedDate(createdDate);
            goal.setTargetDate(targetDate);
            
            // Set priority
            goal.setPriority(String.valueOf(random.nextInt(3) + 1)); // 1-3 priority
            
            goals.add(goal);
        }
        
        context.setGoals(goals);
        
        return context;
    }
} 