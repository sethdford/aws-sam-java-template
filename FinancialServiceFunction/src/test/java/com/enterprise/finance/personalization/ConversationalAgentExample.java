package com.enterprise.finance.personalization;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.enterprise.finance.personalization.model.FinancialContext;
import com.enterprise.finance.personalization.model.UserBehavior;
import com.enterprise.finance.personalization.model.UserPreferences;
import com.enterprise.finance.personalization.service.BedrockService;
import com.enterprise.finance.personalization.service.MockOpenSearchService;
import com.enterprise.finance.personalization.service.PersonalizationConversationalAgent;

/**
 * Example demonstrating the PersonalizationConversationalAgent capabilities.
 * This class creates sample user profiles and initializes the conversational agent.
 */
public class ConversationalAgentExample {

    public static void main(String[] args) {
        System.out.println("Initializing Personalization Conversational Agent Example...");
        
        // Initialize services
        BedrockService bedrockService = new BedrockService();
        MockOpenSearchService openSearchService = new MockOpenSearchService();
        
        // Create the conversational agent
        PersonalizationConversationalAgent agent = new PersonalizationConversationalAgent(bedrockService, openSearchService);
        
        // Create a sample user profile
        String userId = "user-123";
        UserPreferences preferences = createSampleUserPreferences(userId);
        List<UserBehavior> behaviors = createSampleUserBehaviors(userId);
        FinancialContext context = createSampleFinancialContext(userId);
        
        // Start the interactive session
        System.out.println("Starting interactive session with sample user profile...");
        System.out.println("Note: This example requires user input. Follow the prompts to interact with the agent.");
        System.out.println("-------------------------------------------------------------------");
        
        agent.startInteractiveSession(userId, preferences, behaviors, context);
    }
    
    /**
     * Creates a sample user preferences object.
     */
    private static UserPreferences createSampleUserPreferences(String userId) {
        UserPreferences preferences = new UserPreferences();
        preferences.setUserId(userId);
        preferences.setRiskTolerance(7); // Moderate-high risk tolerance (1-10 scale)
        preferences.setInvestmentHorizon("10-15"); // 10-15 years investment horizon
        preferences.setPreferredCategories(Arrays.asList("STOCKS", "ETFs", "MUTUAL_FUNDS"));
        return preferences;
    }
    
    /**
     * Creates sample user behaviors.
     */
    private static List<UserBehavior> createSampleUserBehaviors(String userId) {
        List<UserBehavior> behaviors = new ArrayList<>();
        
        // Add some sample behaviors
        UserBehavior behavior1 = new UserBehavior();
        behavior1.setUserId(userId);
        behavior1.setActionType("VIEW");
        behavior1.setResourceType("ARTICLE");
        behavior1.setResourceId("article-retirement-planning");
        behavior1.setTimestamp(Instant.now().minusSeconds(86400)); // 1 day ago
        behaviors.add(behavior1);
        
        UserBehavior behavior2 = new UserBehavior();
        behavior2.setUserId(userId);
        behavior2.setActionType("SEARCH");
        behavior2.setResourceType("PRODUCT");
        behavior2.setResourceId("search-etf-low-fee");
        behavior2.setTimestamp(Instant.now().minusSeconds(43200)); // 12 hours ago
        behaviors.add(behavior2);
        
        UserBehavior behavior3 = new UserBehavior();
        behavior3.setUserId(userId);
        behavior3.setActionType("CLICK");
        behavior3.setResourceType("CALCULATOR");
        behavior3.setResourceId("calculator-retirement");
        behavior3.setTimestamp(Instant.now().minusSeconds(3600)); // 1 hour ago
        behaviors.add(behavior3);
        
        return behaviors;
    }
    
    /**
     * Creates a sample financial context.
     */
    private static FinancialContext createSampleFinancialContext(String userId) {
        FinancialContext context = new FinancialContext();
        context.setUserId(userId);
        
        // Set financial values
        context.setMonthlyIncome(new BigDecimal("8500"));
        context.setMonthlyExpenses(new BigDecimal("5000"));
        context.setTotalSavings(new BigDecimal("75000"));
        context.setTotalDebt(new BigDecimal("120000"));
        context.setCreditScore(new BigDecimal("720"));
        
        // Calculate derived values
        context.setDisposableIncome(context.getMonthlyIncome().subtract(context.getMonthlyExpenses()));
        context.setSavingsRate(context.getDisposableIncome().divide(context.getMonthlyIncome(), 2, BigDecimal.ROUND_HALF_UP));
        context.setDebtToIncomeRatio(context.getTotalDebt().divide(context.getMonthlyIncome().multiply(new BigDecimal("12")), 2, BigDecimal.ROUND_HALF_UP));
        
        // Add accounts
        List<FinancialContext.Account> accounts = new ArrayList<>();
        
        FinancialContext.Account checkingAccount = new FinancialContext.Account();
        checkingAccount.setAccountId("checking-123");
        checkingAccount.setAccountName("Primary Checking");
        checkingAccount.setAccountType("CHECKING");
        checkingAccount.setBalance(new BigDecimal("15000"));
        accounts.add(checkingAccount);
        
        FinancialContext.Account savingsAccount = new FinancialContext.Account();
        savingsAccount.setAccountId("savings-456");
        savingsAccount.setAccountName("Emergency Fund");
        savingsAccount.setAccountType("SAVINGS");
        savingsAccount.setBalance(new BigDecimal("60000"));
        accounts.add(savingsAccount);
        
        FinancialContext.Account investmentAccount = new FinancialContext.Account();
        investmentAccount.setAccountId("investment-789");
        investmentAccount.setAccountName("Brokerage Account");
        investmentAccount.setAccountType("INVESTMENT");
        investmentAccount.setBalance(new BigDecimal("150000"));
        accounts.add(investmentAccount);
        
        FinancialContext.Account mortgageAccount = new FinancialContext.Account();
        mortgageAccount.setAccountId("mortgage-101");
        mortgageAccount.setAccountName("Home Mortgage");
        mortgageAccount.setAccountType("LOAN");
        mortgageAccount.setBalance(new BigDecimal("-350000"));
        accounts.add(mortgageAccount);
        
        context.setAccounts(accounts);
        
        // Add financial goals
        List<FinancialContext.FinancialGoal> goals = new ArrayList<>();
        
        FinancialContext.FinancialGoal retirementGoal = new FinancialContext.FinancialGoal();
        retirementGoal.setGoalId("goal-retirement");
        retirementGoal.setGoalType("RETIREMENT");
        retirementGoal.setDescription("Comfortable retirement at age 60");
        retirementGoal.setTargetAmount(new BigDecimal("2000000"));
        retirementGoal.setCurrentAmount(new BigDecimal("350000"));
        retirementGoal.setCreatedDate(Instant.now().minusSeconds(31536000)); // 1 year ago
        retirementGoal.setTargetDate(Instant.now().plusSeconds(631152000)); // 20 years from now
        retirementGoal.setPriority("1"); // High priority
        goals.add(retirementGoal);
        
        FinancialContext.FinancialGoal educationGoal = new FinancialContext.FinancialGoal();
        educationGoal.setGoalId("goal-education");
        educationGoal.setGoalType("EDUCATION");
        educationGoal.setDescription("College fund for children");
        educationGoal.setTargetAmount(new BigDecimal("200000"));
        educationGoal.setCurrentAmount(new BigDecimal("50000"));
        educationGoal.setCreatedDate(Instant.now().minusSeconds(15768000)); // 6 months ago
        educationGoal.setTargetDate(Instant.now().plusSeconds(315576000)); // 10 years from now
        educationGoal.setPriority("2"); // Medium priority
        goals.add(educationGoal);
        
        context.setGoals(goals);
        
        return context;
    }
} 