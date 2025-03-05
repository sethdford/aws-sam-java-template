package com.enterprise.finance.personalization.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * Model class representing a user's financial context for personalization.
 */
@DynamoDbBean
public class FinancialContext {
    private String userId;
    private BigDecimal monthlyIncome;
    private BigDecimal totalSavings;
    private BigDecimal totalDebt;
    private BigDecimal creditScore;
    private BigDecimal monthlyExpenses;
    private BigDecimal disposableIncome;
    private BigDecimal savingsRate;
    private BigDecimal debtToIncomeRatio;
    private List<Account> accounts;
    private List<FinancialGoal> goals;
    private Map<String, Object> additionalAttributes;
    private Instant lastUpdated;
    private Portfolio portfolio;
    private List<Account> taxableAccounts;
    private List<Account> taxDeferredAccounts;
    private BigDecimal annualIncome;
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;

    public FinancialContext() {
        this.accounts = new ArrayList<>();
        this.goals = new ArrayList<>();
        this.additionalAttributes = new HashMap<>();
        this.lastUpdated = Instant.now();
        this.taxableAccounts = new ArrayList<>();
        this.taxDeferredAccounts = new ArrayList<>();
    }

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getTotalSavings() {
        return totalSavings;
    }

    public void setTotalSavings(BigDecimal totalSavings) {
        this.totalSavings = totalSavings;
    }

    public BigDecimal getTotalDebt() {
        return totalDebt;
    }

    public void setTotalDebt(BigDecimal totalDebt) {
        this.totalDebt = totalDebt;
    }

    public BigDecimal getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(BigDecimal creditScore) {
        this.creditScore = creditScore;
    }

    public BigDecimal getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public void setMonthlyExpenses(BigDecimal monthlyExpenses) {
        this.monthlyExpenses = monthlyExpenses;
    }

    public BigDecimal getDisposableIncome() {
        return disposableIncome;
    }

    public void setDisposableIncome(BigDecimal disposableIncome) {
        this.disposableIncome = disposableIncome;
    }

    public BigDecimal getSavingsRate() {
        return savingsRate;
    }

    public void setSavingsRate(BigDecimal savingsRate) {
        this.savingsRate = savingsRate;
    }

    public BigDecimal getDebtToIncomeRatio() {
        return debtToIncomeRatio;
    }

    public void setDebtToIncomeRatio(BigDecimal debtToIncomeRatio) {
        this.debtToIncomeRatio = debtToIncomeRatio;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public void addAccount(Account account) {
        this.accounts.add(account);
    }

    public List<FinancialGoal> getGoals() {
        return goals;
    }

    public void setGoals(List<FinancialGoal> goals) {
        this.goals = goals;
    }

    public void addGoal(FinancialGoal goal) {
        this.goals.add(goal);
    }

    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(Map<String, Object> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public void addAttribute(String key, Object value) {
        this.additionalAttributes.put(key, value);
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public List<Account> getTaxableAccounts() {
        return taxableAccounts;
    }

    public void setTaxableAccounts(List<Account> taxableAccounts) {
        this.taxableAccounts = taxableAccounts;
    }

    public List<Account> getTaxDeferredAccounts() {
        return taxDeferredAccounts;
    }

    public void setTaxDeferredAccounts(List<Account> taxDeferredAccounts) {
        this.taxDeferredAccounts = taxDeferredAccounts;
    }

    public BigDecimal getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(BigDecimal annualIncome) {
        this.annualIncome = annualIncome;
    }

    public BigDecimal getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(BigDecimal totalAssets) {
        this.totalAssets = totalAssets;
    }

    public BigDecimal getTotalLiabilities() {
        return totalLiabilities;
    }

    public void setTotalLiabilities(BigDecimal totalLiabilities) {
        this.totalLiabilities = totalLiabilities;
    }

    /**
     * Nested class representing a financial account.
     */
    @DynamoDbBean
    public static class Account {
        private String accountId;
        private String accountType;
        private String accountName;
        private BigDecimal balance;
        private List<FinancialProduct> holdings;
        private boolean taxAdvantaged;
        private Map<String, Object> additionalAttributes;

        // Constructor
        public Account() {
            this.holdings = new ArrayList<>();
            this.additionalAttributes = new HashMap<>();
        }

        // Getters and setters
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }

        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }

        public String getAccountName() { return accountName; }
        public void setAccountName(String accountName) { this.accountName = accountName; }

        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }

        public List<FinancialProduct> getHoldings() { return holdings; }
        public void setHoldings(List<FinancialProduct> holdings) { this.holdings = holdings; }

        public boolean isTaxAdvantaged() { return taxAdvantaged; }
        public void setTaxAdvantaged(boolean taxAdvantaged) { this.taxAdvantaged = taxAdvantaged; }

        public Map<String, Object> getAdditionalAttributes() { return additionalAttributes; }
        public void setAdditionalAttributes(Map<String, Object> additionalAttributes) { this.additionalAttributes = additionalAttributes; }

        // Helper methods
        public void addHolding(FinancialProduct product) {
            if (this.holdings == null) {
                this.holdings = new ArrayList<>();
            }
            this.holdings.add(product);
        }
    }

    /**
     * Nested class representing a financial goal.
     */
    @DynamoDbBean
    public static class FinancialGoal {
        private String goalId;
        private String goalType;
        private String description;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount;
        private Instant targetDate;
        private Instant createdDate;
        private String status;
        private String priority; // HIGH, MEDIUM, LOW
        private Map<String, Object> additionalAttributes;
        
        // Vanguard-specific goal types
        public static final String RETIREMENT = "RETIREMENT";
        public static final String EDUCATION = "EDUCATION";
        public static final String HOME_PURCHASE = "HOME_PURCHASE";
        public static final String WEALTH_TRANSFER = "WEALTH_TRANSFER";
        public static final String EMERGENCY_FUND = "EMERGENCY_FUND";
        public static final String MAJOR_PURCHASE = "MAJOR_PURCHASE";
        public static final String DEBT_PAYOFF = "DEBT_PAYOFF";

        public FinancialGoal() {
            this.createdDate = Instant.now();
            this.status = "ACTIVE";
            this.priority = "MEDIUM";
            this.additionalAttributes = new HashMap<>();
        }

        public String getGoalId() {
            return goalId;
        }

        public void setGoalId(String goalId) {
            this.goalId = goalId;
        }

        public String getGoalType() {
            return goalType;
        }

        public void setGoalType(String goalType) {
            this.goalType = goalType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getTargetAmount() {
            return targetAmount;
        }

        public void setTargetAmount(BigDecimal targetAmount) {
            this.targetAmount = targetAmount;
        }

        public BigDecimal getCurrentAmount() {
            return currentAmount;
        }

        public void setCurrentAmount(BigDecimal currentAmount) {
            this.currentAmount = currentAmount;
        }

        public Instant getTargetDate() {
            return targetDate;
        }

        public void setTargetDate(Instant targetDate) {
            this.targetDate = targetDate;
        }

        public Instant getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(Instant createdDate) {
            this.createdDate = createdDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getPriority() {
            return priority;
        }
        
        public void setPriority(String priority) {
            this.priority = priority;
        }
        
        public Map<String, Object> getAdditionalAttributes() {
            return additionalAttributes;
        }
        
        public void setAdditionalAttributes(Map<String, Object> additionalAttributes) {
            this.additionalAttributes = additionalAttributes;
        }
        
        public void addAttribute(String key, Object value) {
            this.additionalAttributes.put(key, value);
        }
        
        /**
         * Calculates the progress percentage towards the goal.
         * 
         * @return The percentage of progress towards the goal
         */
        public BigDecimal getProgressPercentage() {
            if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            
            if (currentAmount == null) {
                return BigDecimal.ZERO;
            }
            
            return currentAmount.divide(targetAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        
        /**
         * Calculates the remaining amount needed to reach the goal.
         * 
         * @return The remaining amount needed
         */
        public BigDecimal getRemainingAmount() {
            if (targetAmount == null) {
                return BigDecimal.ZERO;
            }
            
            if (currentAmount == null) {
                return targetAmount;
            }
            
            return targetAmount.subtract(currentAmount).max(BigDecimal.ZERO);
        }
        
        /**
         * Calculates the time remaining until the target date.
         * 
         * @return The number of days remaining until the target date
         */
        public long getDaysRemaining() {
            if (targetDate == null) {
                return 0;
            }
            
            return ChronoUnit.DAYS.between(Instant.now(), targetDate);
        }
        
        /**
         * Determines if the goal is on track based on progress and time remaining.
         * 
         * @return true if the goal is on track, false otherwise
         */
        public boolean isOnTrack() {
            if (targetDate == null || targetAmount == null || currentAmount == null) {
                return false;
            }
            
            long totalDays = ChronoUnit.DAYS.between(createdDate, targetDate);
            long daysElapsed = ChronoUnit.DAYS.between(createdDate, Instant.now());
            
            if (totalDays <= 0) {
                return false;
            }
            
            BigDecimal expectedProgress = new BigDecimal(daysElapsed)
                    .divide(new BigDecimal(totalDays), 4, RoundingMode.HALF_UP);
            BigDecimal actualProgress = currentAmount.divide(targetAmount, 4, RoundingMode.HALF_UP);
            
            return actualProgress.compareTo(expectedProgress) >= 0;
        }
    }

    /**
     * Inner class representing a portfolio of financial holdings.
     */
    public static class Portfolio {
        private String portfolioId;
        private BigDecimal totalValue;
        private double riskScore;
        private Map<String, Double> assetAllocation;
        private List<FinancialProduct> holdings;

        public Portfolio() {
            this.assetAllocation = new HashMap<>();
            this.holdings = new ArrayList<>();
        }

        public String getPortfolioId() {
            return portfolioId;
        }

        public void setPortfolioId(String portfolioId) {
            this.portfolioId = portfolioId;
        }

        public BigDecimal getTotalValue() {
            return totalValue;
        }

        public void setTotalValue(BigDecimal totalValue) {
            this.totalValue = totalValue;
        }

        public double getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(double riskScore) {
            this.riskScore = riskScore;
        }

        public Map<String, Double> getAssetAllocation() {
            return assetAllocation;
        }

        public void setAssetAllocation(Map<String, Double> assetAllocation) {
            this.assetAllocation = assetAllocation;
        }

        public List<FinancialProduct> getHoldings() {
            return holdings;
        }

        public void setHoldings(List<FinancialProduct> holdings) {
            this.holdings = holdings;
        }

        public void addHolding(FinancialProduct product) {
            this.holdings.add(product);
        }
    }
} 