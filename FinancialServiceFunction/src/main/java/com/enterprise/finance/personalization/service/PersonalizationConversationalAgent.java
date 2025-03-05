package com.enterprise.finance.personalization.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enterprise.finance.personalization.metrics.PersonalizationMetrics;
import com.enterprise.finance.personalization.model.FinancialContext;
import com.enterprise.finance.personalization.model.FinancialContext.FinancialGoal;
import com.enterprise.finance.personalization.model.UserBehavior;
import com.enterprise.finance.personalization.model.UserPreferences;
import com.enterprise.finance.personalization.service.NextBestActionService.ActionRecommendation;

/**
 * Enhanced conversational agent providing personalized financial advice, behavioral nudges,
 * life event predictions, and dynamic risk profiling based on Vanguard's "Value of Personalized Advice"
 * research and other industry best practices.
 */
public class PersonalizationConversationalAgent {
    private static final Logger logger = LoggerFactory.getLogger(PersonalizationConversationalAgent.class);

    private final NextBestActionService nextBestActionService;
    private final PersonalizationMetrics metrics;
    
    // NLP patterns for understanding user intent
    private final Map<String, Pattern> intentPatterns;
    
    // Behavioral nudge templates
    private final List<String> behavioralNudges;
    
    // Life event prediction templates
    private final Map<String, String> lifeEventInsights;

    public PersonalizationConversationalAgent(BedrockService bedrockService, MockOpenSearchService openSearchService) {
        this.nextBestActionService = new NextBestActionService(bedrockService, openSearchService);
        this.metrics = PersonalizationMetrics.getInstance();
        this.intentPatterns = initializeIntentPatterns();
        this.behavioralNudges = initializeBehavioralNudges();
        this.lifeEventInsights = initializeLifeEventInsights();
        logger.info("PersonalizationConversationalAgent initialized with enhanced capabilities");
    }
    
    /**
     * Initialize patterns for natural language understanding.
     */
    private Map<String, Pattern> initializeIntentPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        // Patterns for understanding user intent
        patterns.put("RETIREMENT", Pattern.compile("(?i).*(retire|retirement|pension).*"));
        patterns.put("INVESTMENT", Pattern.compile("(?i).*(invest|stock|bond|etf|mutual fund|portfolio).*"));
        patterns.put("SAVINGS", Pattern.compile("(?i).*(save|saving|emergency fund).*"));
        patterns.put("DEBT", Pattern.compile("(?i).*(debt|loan|mortgage|credit card).*"));
        patterns.put("EDUCATION", Pattern.compile("(?i).*(education|college|university|student|tuition).*"));
        patterns.put("RISK", Pattern.compile("(?i).*(risk|volatile|safe|conservative|aggressive).*"));
        patterns.put("TAX", Pattern.compile("(?i).*(tax|taxes|deduction|write-off).*"));
        
        return patterns;
    }
    
    /**
     * Initialize behavioral nudge templates based on behavioral economics principles.
     */
    private List<String> initializeBehavioralNudges() {
        List<String> nudges = new ArrayList<>();
        
        // Nudges based on behavioral economics principles
        nudges.add("Did you know that people who set specific financial goals are twice as likely to achieve them?");
        nudges.add("Users who increased their savings rate by just 1% saw significant improvements in their long-term financial health.");
        nudges.add("Consider the 50/30/20 rule: 50% of income for needs, 30% for wants, and 20% for savings and debt repayment.");
        nudges.add("Small, consistent actions often lead to better outcomes than occasional large changes.");
        nudges.add("Many successful investors automate their contributions to avoid emotional decision-making.");
        nudges.add("Research shows that reviewing your financial plan quarterly leads to better long-term outcomes.");
        nudges.add("People who track their expenses tend to save 20% more than those who don't.");
        
        return nudges;
    }
    
    /**
     * Initialize life event insight templates.
     */
    private Map<String, String> initializeLifeEventInsights() {
        Map<String, String> insights = new HashMap<>();
        
        // Insights for different life events
        insights.put("CAREER_CHANGE", "Career changes often present opportunities to reassess retirement contributions and insurance needs.");
        insights.put("HOME_PURCHASE", "Home purchases typically require adjusting emergency funds and considering new insurance needs.");
        insights.put("MARRIAGE", "Marriage is a good time to consolidate accounts, review beneficiaries, and align financial goals.");
        insights.put("CHILDREN", "Having children often necessitates education planning and increased life insurance coverage.");
        insights.put("RETIREMENT_APPROACHING", "As retirement approaches, consider shifting to more conservative investments and reviewing withdrawal strategies.");
        insights.put("INHERITANCE", "Inheritances provide opportunities for debt reduction, increased savings, or strategic investments.");
        
        return insights;
    }

    /**
     * Starts an enhanced interactive session with the user.
     */
    public void startInteractiveSession(String userId, UserPreferences preferences, List<UserBehavior> behaviors, FinancialContext context) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to your enhanced personalized financial advisor!");
        System.out.println("I'm here to provide personalized recommendations based on your unique financial situation.");
        System.out.println("My advice is informed by research showing that personalized financial guidance can lead to 3-4% higher returns.");
        
        // Provide initial personalized insight
        System.out.println("\n" + generatePersonalizedInsight(preferences, behaviors, context));
        
        boolean continueSession = true;
        while (continueSession) {
            // Predict potential life events
            String lifeEventInsight = predictLifeEvents(preferences, behaviors, context);
            if (lifeEventInsight != null) {
                System.out.println("\nLife Event Insight: " + lifeEventInsight);
            }
            
            // Dynamic risk profiling
            adjustRiskProfile(preferences, behaviors, context);
            
            // Get personalized recommendations
            List<ActionRecommendation> recommendations = nextBestActionService.determineNextBestActions(
                    userId, preferences, behaviors, context, 3, NextBestActionService.STRATEGY_HYBRID);
            
            // Display recommendations with behavioral nudges
            System.out.println("\nHere are your personalized recommendations:");
            for (int i = 0; i < recommendations.size(); i++) {
                ActionRecommendation rec = recommendations.get(i);
                System.out.printf("%d. %s (Confidence: %.2f%%)\n", i + 1, rec.getDescription(), rec.getConfidenceScore() * 100);
                System.out.println("   Context: " + rec.getContext());
                
                // Add related products if available
                if (rec.getRelatedProducts() != null && !rec.getRelatedProducts().isEmpty()) {
                    System.out.println("   Related Products:");
                    rec.getRelatedProducts().forEach(product -> 
                        System.out.println("   - " + product.getName() + ": " + product.getDescription()));
                }
            }
            
            // Display a behavioral nudge
            System.out.println("\nBehavioral Insight: " + getRandomBehavioralNudge());
            
            // Get user input
            System.out.println("\nWhat would you like to know more about? (Enter a number, ask a question, or type 'exit' to end)");
            String userInput = scanner.nextLine().trim();
            
            if (userInput.equalsIgnoreCase("exit")) {
                continueSession = false;
                continue;
            }
            
            // Process user input
            if (userInput.matches("\\d+")) {
                // User selected a recommendation by number
                int choice = Integer.parseInt(userInput);
                if (choice > 0 && choice <= recommendations.size()) {
                    handleRecommendationSelection(userId, recommendations.get(choice - 1), scanner);
                } else {
                    System.out.println("Invalid selection. Please try again.");
                }
            } else {
                // User asked a question - use NLP to understand intent
                String intent = determineUserIntent(userInput);
                if (intent != null) {
                    System.out.println(generateResponseForIntent(intent, preferences, behaviors, context));
                } else {
                    System.out.println("I'm not sure I understand. Could you rephrase or select a recommendation number?");
                }
            }
            
            System.out.println("\nWould you like additional recommendations or insights? (yes/no)");
            continueSession = scanner.nextLine().trim().equalsIgnoreCase("yes");
        }
        
        System.out.println("\nThank you for engaging with your enhanced personalized financial advisor.");
        System.out.println("Research shows that consistent engagement with personalized advice can lead to significantly better financial outcomes.");
        System.out.println("\nHere's your detailed session summary:");
        System.out.println(nextBestActionService.generateMetricsReport());
        
        scanner.close();
    }
    
    /**
     * Handles the selection of a recommendation.
     */
    private void handleRecommendationSelection(String userId, ActionRecommendation recommendation, Scanner scanner) {
        System.out.println("\nYou selected: " + recommendation.getDescription());
        System.out.println("This recommendation is based on your financial profile and aligns with industry best practices.");
        System.out.println("Would you like to implement this recommendation? (yes/no)");
        
        String response = scanner.nextLine().trim();
        if (response.equalsIgnoreCase("yes")) {
            nextBestActionService.recordRecommendationAccepted(userId, recommendation, NextBestActionService.STRATEGY_HYBRID);
            
            System.out.println("Great choice! Research shows that following personalized recommendations can lead to better financial outcomes.");
            System.out.println("On a scale of 1-5, how satisfied are you with this recommendation?");
            
            int satisfaction = 0;
            while (satisfaction < 1 || satisfaction > 5) {
                try {
                    satisfaction = Integer.parseInt(scanner.nextLine().trim());
                    if (satisfaction < 1 || satisfaction > 5) {
                        System.out.println("Please enter a number between 1 and 5.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number between 1 and 5.");
                }
            }
            
            nextBestActionService.recordUserSatisfaction(userId, recommendation, satisfaction);
            
            System.out.println("Could you estimate the potential financial impact of this recommendation in dollars?");
            System.out.println("(Enter a rough estimate or 0 if unsure)");
            
            BigDecimal impact = BigDecimal.ZERO;
            try {
                impact = new BigDecimal(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Using default value of $0.");
            }
            
            nextBestActionService.recordFinancialImpact(userId, recommendation, impact);
            
            System.out.println("Thank you! Your feedback helps us improve your personalized experience.");
            System.out.println("We'll track the effectiveness of this recommendation over time.");
        } else {
            System.out.println("No problem. Let's explore other options that might better suit your needs.");
        }
    }
    
    /**
     * Determines user intent from natural language input.
     */
    private String determineUserIntent(String userInput) {
        for (Map.Entry<String, Pattern> entry : intentPatterns.entrySet()) {
            Matcher matcher = entry.getValue().matcher(userInput);
            if (matcher.matches()) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Generates a response based on the determined user intent.
     */
    private String generateResponseForIntent(String intent, UserPreferences preferences, List<UserBehavior> behaviors, FinancialContext context) {
        StringBuilder response = new StringBuilder("Based on your financial profile and the latest research on personalized advice:\n\n");
        
        switch (intent) {
            case "RETIREMENT":
                response.append("Retirement Planning Insights:\n");
                response.append("- Research shows that personalized retirement planning can increase retirement readiness by up to 30%.\n");
                
                // Add personalized retirement insights
                if (context.getGoals() != null) {
                    for (FinancialGoal goal : context.getGoals()) {
                        if ("RETIREMENT".equals(goal.getGoalType())) {
                            BigDecimal progressPercentage = goal.getCurrentAmount()
                                    .divide(goal.getTargetAmount(), 2, BigDecimal.ROUND_HALF_UP)
                                    .multiply(new BigDecimal("100"));
                            
                            response.append(String.format("- You're currently at %.1f%% of your retirement goal of $%s.\n", 
                                    progressPercentage.doubleValue(), goal.getTargetAmount()));
                            
                            // Calculate required monthly contribution
                            if (goal.getTargetDate() != null) {
                                LocalDate targetDate = goal.getTargetDate().atZone(ZoneId.systemDefault()).toLocalDate();
                                LocalDate now = LocalDate.now();
                                
                                // Fix: Use ChronoUnit.MONTHS.between() instead of Period.between().toTotalMonths()
                                long monthsRemainingLong = java.time.temporal.ChronoUnit.MONTHS.between(now, targetDate);
                                // Ensure we don't lose precision in a way that matters for this calculation
                                int monthsRemaining = (monthsRemainingLong > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) monthsRemainingLong;
                                
                                if (monthsRemaining > 0) {
                                    BigDecimal amountNeeded = goal.getTargetAmount().subtract(goal.getCurrentAmount());
                                    BigDecimal monthlyContribution = amountNeeded.divide(new BigDecimal(monthsRemaining), 2, BigDecimal.ROUND_HALF_UP);
                                    
                                    response.append(String.format("- To reach your goal, consider contributing approximately $%s monthly.\n", 
                                            monthlyContribution));
                                }
                            }
                        }
                    }
                }
                
                // Add risk tolerance insight
                response.append(String.format("- With your risk tolerance of %d/10, you might consider a portfolio with ", 
                        preferences.getRiskTolerance()));
                
                if (preferences.getRiskTolerance() >= 8) {
                    response.append("70-80% stocks and 20-30% bonds.\n");
                } else if (preferences.getRiskTolerance() >= 5) {
                    response.append("60% stocks and 40% bonds.\n");
                } else {
                    response.append("40% stocks and 60% bonds.\n");
                }
                
                break;
                
            case "INVESTMENT":
                response.append("Investment Insights:\n");
                response.append("- Personalized investment strategies can lead to 3-4% higher returns compared to generic approaches.\n");
                
                // Add personalized investment insights
                response.append(String.format("- With your risk tolerance of %d/10 and investment horizon of %s years, ", 
                        preferences.getRiskTolerance(), preferences.getInvestmentHorizon()));
                
                if (preferences.getRiskTolerance() >= 7) {
                    response.append("you might benefit from a growth-oriented portfolio.\n");
                } else if (preferences.getRiskTolerance() >= 4) {
                    response.append("a balanced portfolio may be appropriate.\n");
                } else {
                    response.append("a conservative portfolio focused on capital preservation may be suitable.\n");
                }
                
                // Add preferred categories insight
                if (preferences.getPreferredCategories() != null && !preferences.getPreferredCategories().isEmpty()) {
                    response.append("- Your interest in " + String.join(", ", preferences.getPreferredCategories()) + 
                            " aligns with your risk profile.\n");
                }
                
                break;
                
            case "SAVINGS":
                response.append("Savings Insights:\n");
                response.append("- Research shows that personalized savings strategies can increase savings rates by up to 5%.\n");
                
                // Add personalized savings insights
                if (context.getSavingsRate() != null) {
                    BigDecimal savingsRatePercentage = context.getSavingsRate().multiply(new BigDecimal("100"));
                    response.append(String.format("- Your current savings rate is %.1f%%. ", savingsRatePercentage.doubleValue()));
                    
                    if (savingsRatePercentage.compareTo(new BigDecimal("20")) < 0) {
                        response.append("Consider gradually increasing to 20% for optimal long-term financial health.\n");
                    } else {
                        response.append("This is excellent and exceeds the recommended 20% guideline.\n");
                    }
                }
                
                // Add emergency fund insight
                if (context.getTotalSavings() != null && context.getMonthlyExpenses() != null) {
                    BigDecimal monthsCovered = context.getTotalSavings().divide(context.getMonthlyExpenses(), 1, BigDecimal.ROUND_HALF_UP);
                    response.append(String.format("- Your emergency fund covers approximately %.1f months of expenses. ", 
                            monthsCovered.doubleValue()));
                    
                    if (monthsCovered.compareTo(new BigDecimal("6")) < 0) {
                        response.append("Consider building this to 6 months for optimal protection.\n");
                    } else {
                        response.append("This provides excellent financial security.\n");
                    }
                }
                
                break;
                
            case "DEBT":
                response.append("Debt Management Insights:\n");
                response.append("- Personalized debt reduction strategies can accelerate debt payoff by up to 30%.\n");
                
                // Add personalized debt insights
                if (context.getDebtToIncomeRatio() != null) {
                    BigDecimal dtiPercentage = context.getDebtToIncomeRatio().multiply(new BigDecimal("100"));
                    response.append(String.format("- Your debt-to-income ratio is %.1f%%. ", dtiPercentage.doubleValue()));
                    
                    if (dtiPercentage.compareTo(new BigDecimal("36")) > 0) {
                        response.append("Reducing this below 36% could improve your financial flexibility and credit options.\n");
                    } else {
                        response.append("This is within the recommended range and indicates good financial health.\n");
                    }
                }
                
                // Add specific debt insights
                if (context.getAccounts() != null) {
                    boolean hasHighInterestDebt = false;
                    for (FinancialContext.Account account : context.getAccounts()) {
                        if ("LOAN".equals(account.getAccountType()) || "CREDIT_CARD".equals(account.getAccountType())) {
                            hasHighInterestDebt = true;
                            break;
                        }
                    }
                    
                    if (hasHighInterestDebt) {
                        response.append("- Consider prioritizing high-interest debt for faster payoff and interest savings.\n");
                    }
                }
                
                break;
                
            default:
                response.append("I don't have specific insights on that topic yet, but I can help with retirement planning, investments, savings, and debt management.\n");
                response.append("Would you like to explore one of these areas?");
        }
        
        return response.toString();
    }
    
    /**
     * Predicts potential life events based on user profile.
     */
    private String predictLifeEvents(UserPreferences preferences, List<UserBehavior> behaviors, FinancialContext context) {
        // Simple life event prediction based on financial context
        if (context.getGoals() != null) {
            for (FinancialGoal goal : context.getGoals()) {
                // Check for retirement approaching
                if ("RETIREMENT".equals(goal.getGoalType()) && goal.getTargetDate() != null) {
                    LocalDate targetDate = goal.getTargetDate().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate now = LocalDate.now();
                    Period period = Period.between(now, targetDate);
                    int yearsToRetirement = period.getYears();
                    
                    if (yearsToRetirement <= 5) {
                        return lifeEventInsights.get("RETIREMENT_APPROACHING");
                    }
                }
                
                // Check for education planning (children)
                if ("EDUCATION".equals(goal.getGoalType())) {
                    return lifeEventInsights.get("CHILDREN");
                }
            }
        }
        
        // Check for home purchase planning
        if (behaviors != null) {
            for (UserBehavior behavior : behaviors) {
                if (behavior.getResourceId() != null && 
                    behavior.getResourceId().toLowerCase().contains("mortgage") || 
                    behavior.getResourceId().toLowerCase().contains("house") ||
                    behavior.getResourceId().toLowerCase().contains("home")) {
                    return lifeEventInsights.get("HOME_PURCHASE");
                }
            }
        }
        
        return null;
    }
    
    /**
     * Adjusts risk profile based on market conditions and user situation.
     */
    private void adjustRiskProfile(UserPreferences preferences, List<UserBehavior> behaviors, FinancialContext context) {
        // This would typically connect to market data and adjust risk profile dynamically
        // For this example, we'll just log that the risk profile was evaluated
        logger.info("Evaluated risk profile for user: {}. Current risk tolerance: {}", 
                preferences.getUserId(), preferences.getRiskTolerance());
    }
    
    /**
     * Generates a personalized insight based on the user's financial profile.
     */
    private String generatePersonalizedInsight(UserPreferences preferences, List<UserBehavior> behaviors, FinancialContext context) {
        StringBuilder insight = new StringBuilder("Personalized Financial Insight: ");
        
        // Generate insight based on most pressing financial need
        if (context.getSavingsRate() != null && context.getSavingsRate().compareTo(new BigDecimal("0.2")) < 0) {
            insight.append("Increasing your savings rate could significantly improve your long-term financial health. ");
            insight.append("Research shows that a 20% savings rate is optimal for most individuals.");
        } else if (context.getDebtToIncomeRatio() != null && context.getDebtToIncomeRatio().compareTo(new BigDecimal("0.36")) > 0) {
            insight.append("Reducing your debt-to-income ratio below 36% could improve your financial flexibility. ");
            insight.append("This is a key metric that lenders use to evaluate financial health.");
        } else if (context.getTotalSavings() != null && context.getMonthlyExpenses() != null) {
            BigDecimal monthsCovered = context.getTotalSavings().divide(context.getMonthlyExpenses(), 1, BigDecimal.ROUND_HALF_UP);
            if (monthsCovered.compareTo(new BigDecimal("6")) < 0) {
                insight.append("Building your emergency fund to cover 6 months of expenses could provide important financial security. ");
                insight.append("Currently, you have approximately " + monthsCovered + " months covered.");
            } else {
                insight.append("Your financial foundation looks solid with a good emergency fund and healthy savings rate. ");
                insight.append("You may be ready to focus on optimizing investments for long-term growth.");
            }
        } else {
            insight.append("Based on your profile, personalized financial advice could help you optimize your financial strategy. ");
            insight.append("Research shows this can lead to 3-4% higher returns over time.");
        }
        
        return insight.toString();
    }
    
    /**
     * Returns a random behavioral nudge.
     */
    private String getRandomBehavioralNudge() {
        int index = (int) (Math.random() * behavioralNudges.size());
        return behavioralNudges.get(index);
    }
} 