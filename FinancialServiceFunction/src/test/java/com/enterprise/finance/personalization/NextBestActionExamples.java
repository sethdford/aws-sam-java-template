package com.enterprise.finance.personalization;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.enterprise.finance.personalization.model.FinancialContext;
import com.enterprise.finance.personalization.model.FinancialProduct;
import com.enterprise.finance.personalization.model.UserBehavior;
import com.enterprise.finance.personalization.model.UserPreferences;
import com.enterprise.finance.personalization.service.BedrockService;
import com.enterprise.finance.personalization.service.MockOpenSearchService;
import com.enterprise.finance.personalization.service.NextBestActionService;

/**
 * Examples of using the NextBestActionService with different user profiles.
 */
public class NextBestActionExamples {

    public static void main(String[] args) {
        System.out.println("Starting Next Best Action Examples");
        
        // Initialize services
        BedrockService bedrockService = new BedrockService();
        MockOpenSearchService openSearchService = new MockOpenSearchService();
        NextBestActionService nextBestActionService = new NextBestActionService(bedrockService, openSearchService);
        
        // Example 1: Conservative User
        System.out.println("\nExample 1: Conservative User");
        System.out.println("----------------------------");
        
        UserPreferences conservativeUser = createConservativeUserPreferences();
        List<UserBehavior> conservativeBehaviors = createConservativeUserBehaviors();
        FinancialContext conservativeContext = createConservativeFinancialContext();
        
        List<NextBestActionService.ActionRecommendation> conservativeRecommendations = 
            nextBestActionService.determineNextBestActions(conservativeUser, conservativeBehaviors, conservativeContext);
        
        printRecommendations(conservativeRecommendations);
        
        // Example 2: Aggressive User
        System.out.println("\nExample 2: Aggressive User");
        System.out.println("--------------------------");
        
        UserPreferences aggressiveUser = createAggressiveUserPreferences();
        List<UserBehavior> aggressiveBehaviors = createAggressiveUserBehaviors();
        FinancialContext aggressiveContext = createAggressiveFinancialContext();
        
        List<NextBestActionService.ActionRecommendation> aggressiveRecommendations = 
            nextBestActionService.determineNextBestActions(aggressiveUser, aggressiveBehaviors, aggressiveContext);
        
        printRecommendations(aggressiveRecommendations);
        
        // Example 3: User Experiencing Life Event (Job Change)
        System.out.println("\nExample 3: User Experiencing Life Event (Job Change)");
        System.out.println("-----------------------------------------------------");
        
        UserPreferences lifeEventUser = createModerateUserPreferences();
        List<UserBehavior> lifeEventBehaviors = createLifeEventUserBehaviors();
        FinancialContext lifeEventContext = createModerateFinancialContext();
        
        List<NextBestActionService.ActionRecommendation> lifeEventRecommendations = 
            nextBestActionService.determineNextBestActions(lifeEventUser, lifeEventBehaviors, lifeEventContext);
        
        printRecommendations(lifeEventRecommendations);
    }
    
    private static void printRecommendations(List<NextBestActionService.ActionRecommendation> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            System.out.println("No recommendations generated.");
            return;
        }
        
        for (int i = 0; i < recommendations.size(); i++) {
            NextBestActionService.ActionRecommendation rec = recommendations.get(i);
            if (rec == null) {
                System.out.println((i + 1) + ". [NULL RECOMMENDATION]");
                continue;
            }
            
            System.out.println((i + 1) + ". " + 
                (rec.getActionType() != null ? rec.getActionType() : "UNKNOWN") + 
                " (" + String.format("%.1f", rec.getConfidenceScore() * 100) + "% confidence)");
            
            System.out.println("   Description: " + (rec.getDescription() != null ? rec.getDescription() : "N/A"));
            System.out.println("   Context: " + (rec.getContext() != null ? rec.getContext() : "N/A"));
            
            if (rec.getRelatedProducts() != null && !rec.getRelatedProducts().isEmpty()) {
                System.out.println("   Related Products:");
                for (FinancialProduct product : rec.getRelatedProducts()) {
                    System.out.println("     - " + product.getName() + 
                        (product.getCategory() != null ? " (" + product.getCategory() + ")" : ""));
                }
            }
            
            System.out.println();
        }
    }
    
    private static UserPreferences createConservativeUserPreferences() {
        UserPreferences prefs = new UserPreferences();
        prefs.setUserId("user123");
        prefs.setRiskTolerance(30); // Conservative risk tolerance (0-100 scale)
        prefs.setInvestmentHorizon("SHORT_TERM");
        
        List<String> preferredCategories = new ArrayList<>();
        preferredCategories.add("FIXED_INCOME");
        preferredCategories.add("DIVIDEND");
        prefs.setPreferredCategories(preferredCategories);
        
        Map<String, Integer> categoryPrefs = new HashMap<>();
        categoryPrefs.put("FIXED_INCOME", 70);
        categoryPrefs.put("DIVIDEND", 30);
        prefs.setCategoryPreferences(categoryPrefs);
        
        return prefs;
    }
    
    private static List<UserBehavior> createConservativeUserBehaviors() {
        List<UserBehavior> behaviors = new ArrayList<>();
        
        UserBehavior b1 = new UserBehavior();
        b1.setUserId("user123");
        b1.setActionType("VIEW");
        b1.setResourceId("BOND_FUND_1");
        b1.setResourceType("PRODUCT");
        b1.setBehaviorId(UUID.randomUUID().toString());
        b1.setTimestamp(Instant.now().minus(5, ChronoUnit.DAYS));
        behaviors.add(b1);
        
        UserBehavior b2 = new UserBehavior();
        b2.setUserId("user123");
        b2.setActionType("BUY");
        b2.setResourceId("TREASURY_ETF");
        b2.setResourceType("PRODUCT");
        b2.setBehaviorId(UUID.randomUUID().toString());
        b2.setTimestamp(Instant.now().minus(30, ChronoUnit.DAYS));
        behaviors.add(b2);
        
        return behaviors;
    }
    
    private static FinancialContext createConservativeFinancialContext() {
        FinancialContext context = new FinancialContext();
        context.setUserId("user123");
        context.setMonthlyIncome(new BigDecimal("5000"));
        context.setTotalSavings(new BigDecimal("50000"));
        context.setTotalDebt(new BigDecimal("10000"));
        context.setCreditScore(new BigDecimal("750"));
        context.setMonthlyExpenses(new BigDecimal("3500"));
        
        // Add retirement goal
        FinancialContext.FinancialGoal retirementGoal = new FinancialContext.FinancialGoal();
        retirementGoal.setGoalId("goal1");
        retirementGoal.setGoalType(FinancialContext.FinancialGoal.RETIREMENT);
        retirementGoal.setDescription("Retirement at 65");
        retirementGoal.setTargetAmount(new BigDecimal("1000000"));
        retirementGoal.setCurrentAmount(new BigDecimal("150000"));
        retirementGoal.setCreatedDate(Instant.now().minus(5 * 365, ChronoUnit.DAYS)); // 5 years ago
        retirementGoal.setTargetDate(Instant.now().plus(20 * 365, ChronoUnit.DAYS)); // 20 years from now
        retirementGoal.setPriority("HIGH");
        context.addGoal(retirementGoal);
        
        // Add emergency fund goal that's on track
        FinancialContext.FinancialGoal emergencyGoal = new FinancialContext.FinancialGoal();
        emergencyGoal.setGoalId("goal2");
        emergencyGoal.setGoalType(FinancialContext.FinancialGoal.EMERGENCY_FUND);
        emergencyGoal.setDescription("6 months of expenses");
        emergencyGoal.setTargetAmount(new BigDecimal("30000"));
        emergencyGoal.setCurrentAmount(new BigDecimal("25000"));
        emergencyGoal.setCreatedDate(Instant.now().minus(1 * 365, ChronoUnit.DAYS)); // 1 year ago
        emergencyGoal.setTargetDate(Instant.now().plus(6 * 30, ChronoUnit.DAYS)); // 6 months from now
        emergencyGoal.setPriority("MEDIUM");
        context.addGoal(emergencyGoal);
        
        // Create portfolio with conservative allocation
        FinancialContext.Portfolio portfolio = new FinancialContext.Portfolio();
        portfolio.setPortfolioId("portfolio1");
        portfolio.setTotalValue(new BigDecimal("200000"));
        portfolio.setRiskScore(30.0);
        
        Map<String, Double> allocation = new HashMap<>();
        allocation.put("BONDS", 0.7);
        allocation.put("STOCKS", 0.2);
        allocation.put("CASH", 0.1);
        portfolio.setAssetAllocation(allocation);
        
        // Add some holdings
        FinancialProduct bond1 = new FinancialProduct();
        bond1.setProductId("BOND_FUND_1");
        bond1.setName("Treasury Bond Fund");
        bond1.setCategory("FIXED_INCOME");
        bond1.setAssetClass("BONDS");
        bond1.setCurrentPrice(new BigDecimal("82.50"));
        bond1.setPurchasePrice(new BigDecimal("85.75"));
        // Note: FinancialProduct doesn't have a setQuantity method that takes BigDecimal
        // We'll use addAttribute instead
        bond1.addAttribute("quantity", new BigDecimal("500"));
        bond1.setPurchaseDate(Instant.now().minus(2 * 365, ChronoUnit.DAYS));
        bond1.setTaxInefficient(false);
        portfolio.addHolding(bond1);
        
        FinancialProduct stock1 = new FinancialProduct();
        stock1.setProductId("STOCK_FUND_1");
        stock1.setName("Dividend Stock Fund");
        stock1.setCategory("DIVIDEND");
        stock1.setAssetClass("STOCKS");
        stock1.setCurrentPrice(new BigDecimal("220.30"));
        stock1.setPurchasePrice(new BigDecimal("195.40"));
        // Note: FinancialProduct doesn't have a setQuantity method that takes BigDecimal
        // We'll use addAttribute instead
        stock1.addAttribute("quantity", new BigDecimal("300"));
        stock1.setPurchaseDate(Instant.now().minus(3 * 365, ChronoUnit.DAYS));
        stock1.setTaxInefficient(false);
        portfolio.addHolding(stock1);
        
        context.setPortfolio(portfolio);
        
        return context;
    }
    
    private static UserPreferences createAggressiveUserPreferences() {
        UserPreferences prefs = new UserPreferences();
        prefs.setUserId("user456");
        prefs.setRiskTolerance(80); // Aggressive risk tolerance (0-100 scale)
        prefs.setInvestmentHorizon("LONG_TERM");
        
        List<String> preferredCategories = new ArrayList<>();
        preferredCategories.add("GROWTH");
        preferredCategories.add("INTERNATIONAL");
        prefs.setPreferredCategories(preferredCategories);
        
        Map<String, Integer> categoryPrefs = new HashMap<>();
        categoryPrefs.put("GROWTH", 60);
        categoryPrefs.put("INTERNATIONAL", 40);
        prefs.setCategoryPreferences(categoryPrefs);
        
        return prefs;
    }
    
    private static List<UserBehavior> createAggressiveUserBehaviors() {
        List<UserBehavior> behaviors = new ArrayList<>();
        
        // Recent sell behaviors
        for (int i = 0; i < 3; i++) {
            UserBehavior b = new UserBehavior();
            b.setUserId("user456");
            b.setActionType("SELL");
            b.setResourceId("STOCK_FUND_" + (i + 1));
            b.setResourceType("PRODUCT");
            b.setBehaviorId(UUID.randomUUID().toString());
            b.setTimestamp(Instant.now().minus(i + 1, ChronoUnit.DAYS));
            behaviors.add(b);
        }
        
        // Frequent trading behavior
        for (int i = 0; i < 10; i++) {
            UserBehavior b = new UserBehavior();
            b.setUserId("user456");
            b.setActionType(i % 2 == 0 ? "BUY" : "SELL");
            b.setResourceId("STOCK_" + (i + 1));
            b.setResourceType("PRODUCT");
            b.setBehaviorId(UUID.randomUUID().toString());
            b.setTimestamp(Instant.now().minus(i + 1, ChronoUnit.DAYS));
            behaviors.add(b);
        }
        
        return behaviors;
    }
    
    private static FinancialContext createAggressiveFinancialContext() {
        FinancialContext context = new FinancialContext();
        context.setUserId("user456");
        context.setMonthlyIncome(new BigDecimal("12000"));
        context.setTotalSavings(new BigDecimal("200000"));
        context.setTotalDebt(new BigDecimal("150000"));
        context.setCreditScore(new BigDecimal("780"));
        context.setMonthlyExpenses(new BigDecimal("8000"));
        
        // Add retirement goal
        FinancialContext.FinancialGoal retirementGoal = new FinancialContext.FinancialGoal();
        retirementGoal.setGoalId("goal1");
        retirementGoal.setGoalType(FinancialContext.FinancialGoal.RETIREMENT);
        retirementGoal.setDescription("Early retirement at 55");
        retirementGoal.setTargetAmount(new BigDecimal("3000000"));
        retirementGoal.setCurrentAmount(new BigDecimal("500000"));
        retirementGoal.setCreatedDate(Instant.now().minus(10 * 365, ChronoUnit.DAYS)); // 10 years ago
        retirementGoal.setTargetDate(Instant.now().plus(15 * 365, ChronoUnit.DAYS)); // 15 years from now
        retirementGoal.setPriority("HIGH");
        context.addGoal(retirementGoal);
        
        // Create portfolio with misaligned allocation (too conservative for aggressive preferences)
        FinancialContext.Portfolio portfolio = new FinancialContext.Portfolio();
        portfolio.setPortfolioId("portfolio1");
        portfolio.setTotalValue(new BigDecimal("700000"));
        portfolio.setRiskScore(40.0); // Too low for aggressive investor
        
        Map<String, Double> allocation = new HashMap<>();
        allocation.put("BONDS", 0.5);
        allocation.put("STOCKS", 0.4);
        allocation.put("CASH", 0.1);
        portfolio.setAssetAllocation(allocation);
        
        // Add some holdings
        FinancialProduct bond1 = new FinancialProduct();
        bond1.setProductId("BOND_FUND_1");
        bond1.setName("Corporate Bond Fund");
        bond1.setCategory("FIXED_INCOME");
        bond1.setAssetClass("BONDS");
        bond1.setCurrentPrice(new BigDecimal("45.25"));
        bond1.setPurchasePrice(new BigDecimal("48.75"));
        // Note: FinancialProduct doesn't have a setQuantity method that takes BigDecimal
        // We'll use addAttribute instead
        bond1.addAttribute("quantity", new BigDecimal("1000"));
        bond1.setPurchaseDate(Instant.now().minus(1 * 365, ChronoUnit.DAYS));
        bond1.setTaxInefficient(true);
        portfolio.addHolding(bond1);
        
        FinancialProduct stock1 = new FinancialProduct();
        stock1.setProductId("STOCK_FUND_1");
        stock1.setName("Growth Stock Fund");
        stock1.setCategory("GROWTH");
        stock1.setAssetClass("STOCKS");
        stock1.setCurrentPrice(new BigDecimal("310.30"));
        stock1.setPurchasePrice(new BigDecimal("280.40"));
        // Note: FinancialProduct doesn't have a setQuantity method that takes BigDecimal
        // We'll use addAttribute instead
        stock1.addAttribute("quantity", new BigDecimal("800"));
        stock1.setPurchaseDate(Instant.now().minus(2 * 365, ChronoUnit.DAYS));
        stock1.setTaxInefficient(false);
        portfolio.addHolding(stock1);
        
        context.setPortfolio(portfolio);
        
        return context;
    }
    
    private static UserPreferences createModerateUserPreferences() {
        UserPreferences prefs = new UserPreferences();
        prefs.setUserId("user789");
        prefs.setRiskTolerance(50); // Moderate risk tolerance (0-100 scale)
        prefs.setInvestmentHorizon("MEDIUM_TERM");
        
        List<String> preferredCategories = new ArrayList<>();
        preferredCategories.add("BALANCED");
        preferredCategories.add("INCOME");
        prefs.setPreferredCategories(preferredCategories);
        
        Map<String, Integer> categoryPrefs = new HashMap<>();
        categoryPrefs.put("BALANCED", 50);
        categoryPrefs.put("INCOME", 50);
        prefs.setCategoryPreferences(categoryPrefs);
        
        return prefs;
    }
    
    private static List<UserBehavior> createLifeEventUserBehaviors() {
        List<UserBehavior> behaviors = new ArrayList<>();
        
        UserBehavior b1 = new UserBehavior();
        b1.setUserId("user789");
        b1.setActionType("UPDATE_INCOME");
        b1.setResourceType("PROFILE");
        b1.setBehaviorId(UUID.randomUUID().toString());
        b1.addMetadata("context", "Salary increase from job change");
        b1.setTimestamp(Instant.now().minus(10, ChronoUnit.DAYS));
        behaviors.add(b1);
        
        UserBehavior b2 = new UserBehavior();
        b2.setUserId("user789");
        b2.setActionType("ROLLOVER_401K");
        b2.setResourceType("ACCOUNT");
        b2.setBehaviorId(UUID.randomUUID().toString());
        b2.addMetadata("context", "401k rollover from previous employer");
        b2.setTimestamp(Instant.now().minus(5, ChronoUnit.DAYS));
        behaviors.add(b2);
        
        UserBehavior b3 = new UserBehavior();
        b3.setUserId("user789");
        b3.setActionType("VIEW");
        b3.setResourceId("BALANCED_FUND_1");
        b3.setResourceType("PRODUCT");
        b3.setBehaviorId(UUID.randomUUID().toString());
        b3.setTimestamp(Instant.now().minus(15, ChronoUnit.DAYS));
        behaviors.add(b3);
        
        UserBehavior b4 = new UserBehavior();
        b4.setUserId("user789");
        b4.setActionType("BUY");
        b4.setResourceId("INCOME_ETF");
        b4.setResourceType("PRODUCT");
        b4.setBehaviorId(UUID.randomUUID().toString());
        b4.setTimestamp(Instant.now().minus(45, ChronoUnit.DAYS));
        behaviors.add(b4);
        
        return behaviors;
    }
    
    private static FinancialContext createModerateFinancialContext() {
        FinancialContext context = new FinancialContext();
        context.setUserId("user789");
        context.setMonthlyIncome(new BigDecimal("8000"));
        context.setTotalSavings(new BigDecimal("120000"));
        context.setTotalDebt(new BigDecimal("80000"));
        context.setCreditScore(new BigDecimal("720"));
        context.setMonthlyExpenses(new BigDecimal("5500"));
        
        // Add retirement goal
        FinancialContext.FinancialGoal retirementGoal = new FinancialContext.FinancialGoal();
        retirementGoal.setGoalId("goal1");
        retirementGoal.setGoalType(FinancialContext.FinancialGoal.RETIREMENT);
        retirementGoal.setDescription("Retirement at 60");
        retirementGoal.setTargetAmount(new BigDecimal("2000000"));
        retirementGoal.setCurrentAmount(new BigDecimal("350000"));
        retirementGoal.setCreatedDate(Instant.now().minus(8 * 365, ChronoUnit.DAYS)); // 8 years ago
        retirementGoal.setTargetDate(Instant.now().plus(18 * 365, ChronoUnit.DAYS)); // 18 years from now
        retirementGoal.setPriority("HIGH");
        context.addGoal(retirementGoal);
        
        // Add home purchase goal
        FinancialContext.FinancialGoal homeGoal = new FinancialContext.FinancialGoal();
        homeGoal.setGoalId("goal2");
        homeGoal.setGoalType(FinancialContext.FinancialGoal.HOME_PURCHASE);
        homeGoal.setDescription("Down payment for home");
        homeGoal.setTargetAmount(new BigDecimal("100000"));
        homeGoal.setCurrentAmount(new BigDecimal("65000"));
        homeGoal.setCreatedDate(Instant.now().minus(3 * 365, ChronoUnit.DAYS)); // 3 years ago
        homeGoal.setTargetDate(Instant.now().plus(1 * 365, ChronoUnit.DAYS)); // 1 year from now
        homeGoal.setPriority("MEDIUM");
        context.addGoal(homeGoal);
        
        // Create balanced portfolio
        FinancialContext.Portfolio portfolio = new FinancialContext.Portfolio();
        portfolio.setPortfolioId("portfolio1");
        portfolio.setTotalValue(new BigDecimal("450000"));
        portfolio.setRiskScore(50.0);
        
        Map<String, Double> allocation = new HashMap<>();
        allocation.put("BONDS", 0.4);
        allocation.put("STOCKS", 0.5);
        allocation.put("CASH", 0.1);
        portfolio.setAssetAllocation(allocation);
        
        context.setPortfolio(portfolio);
        
        return context;
    }
} 