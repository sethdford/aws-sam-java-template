package com.enterprise.finance.personalization;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enterprise.finance.personalization.model.FinancialContext;
import com.enterprise.finance.personalization.model.UserPreferences;
import com.enterprise.finance.personalization.service.BedrockService;
import com.enterprise.finance.personalization.service.MockOpenSearchService;
import com.enterprise.finance.personalization.service.NextBestActionService;

public class NextBestActionSimpleExample {

    public static void main(String[] args) {
        System.out.println("Starting Next Best Action Simple Example");
        
        try {
            // Create services
            BedrockService bedrockService = new BedrockService();
            MockOpenSearchService openSearchService = new MockOpenSearchService();
            NextBestActionService nextBestActionService = new NextBestActionService(bedrockService, openSearchService);
            
            // Create a simple user profile
            UserPreferences userPrefs = new UserPreferences();
            userPrefs.setUserId("simple-user");
            userPrefs.setRiskTolerance(50); // Using a moderate risk tolerance value (0-100 scale)
            
            // Create a simple financial context
            FinancialContext context = new FinancialContext();
            context.setUserId("simple-user");
            
            // Add a retirement goal
            FinancialContext.FinancialGoal goal = new FinancialContext.FinancialGoal();
            goal.setGoalId("retirement");
            goal.setGoalType(FinancialContext.FinancialGoal.RETIREMENT);
            goal.setTargetAmount(new BigDecimal("1000000"));
            goal.setCurrentAmount(new BigDecimal("200000"));
            goal.setCreatedDate(Instant.now().minus(5 * 365, ChronoUnit.DAYS));
            goal.setTargetDate(Instant.now().plus(20 * 365, ChronoUnit.DAYS));
            goal.setPriority("HIGH");
            context.addGoal(goal);
            
            // Create a simple portfolio
            FinancialContext.Portfolio portfolio = new FinancialContext.Portfolio();
            portfolio.setPortfolioId("portfolio1");
            portfolio.setRiskScore(50.0);
            
            Map<String, Double> allocation = new HashMap<>();
            allocation.put("STOCKS", 0.6);
            allocation.put("BONDS", 0.3);
            allocation.put("CASH", 0.1);
            portfolio.setAssetAllocation(allocation);
            
            context.setPortfolio(portfolio);
            
            // Generate recommendations
            List<NextBestActionService.ActionRecommendation> recommendations = 
                nextBestActionService.determineNextBestActions(userPrefs, new ArrayList<>(), context);
            
            // Print recommendations
            System.out.println("Generated " + recommendations.size() + " recommendations:");
            for (int i = 0; i < recommendations.size(); i++) {
                NextBestActionService.ActionRecommendation rec = recommendations.get(i);
                System.out.println((i + 1) + ". " + rec.getActionType() + " (" + 
                    String.format("%.1f", rec.getConfidenceScore() * 100) + "% confidence)");
                System.out.println("   " + rec.getDescription());
            }
            
        } catch (Exception e) {
            System.err.println("Error running example: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 