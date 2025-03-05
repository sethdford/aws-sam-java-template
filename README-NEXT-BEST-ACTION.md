# Next Best Action Feature

The Next Best Action feature is a powerful personalization capability that provides tailored financial recommendations to users based on their preferences, behaviors, and financial context. This feature uses embeddings to match user profiles with appropriate financial actions and products.

## Overview

The Next Best Action feature analyzes a user's financial situation and provides actionable recommendations in several categories:

- **Investment Opportunities**: Suggestions for investing available funds based on risk tolerance and goals.
- **Savings Recommendations**: Advice on improving savings rates and building emergency funds.
- **Debt Management**: Strategies for reducing debt and improving debt-to-income ratios.
- **Credit Improvement**: Tips for improving credit scores and qualifying for better financial products.
- **Budget Optimization**: Recommendations for optimizing spending and saving patterns.
- **Retirement Planning**: Guidance on retirement savings and investment strategies.
- **Tax Optimization**: Suggestions for tax-efficient financial strategies.
- **Financial Education**: Educational resources tailored to the user's financial needs.

Each recommendation includes:
- A description of the recommended action
- Context explaining why the action is recommended
- A confidence score indicating the relevance of the recommendation
- Related financial products that can help implement the recommendation

## Architecture

The Next Best Action feature is implemented using the following components:

1. **NextBestActionHandler**: Lambda function that processes API requests and returns personalized recommendations.
2. **NextBestActionService**: Core service that analyzes user data and generates recommendations.
3. **BedrockService**: Service for generating embeddings from text (mock implementation for development).
4. **MockOpenSearchService**: Service for finding relevant financial products (mock implementation for development).
5. **Model Classes**:
   - `UserPreferences`: Stores user preference data for personalization.
   - `UserBehavior`: Tracks user interactions and behaviors.
   - `FinancialContext`: Represents a user's financial situation.
   - `FinancialProduct`: Represents financial products that can be recommended.

## Testing Locally

To test the Next Best Action feature locally:

1. Make sure you have the AWS SAM CLI installed:
   ```
   sam --version
   ```
   If not installed, follow the instructions at: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html

2. Build the project:
   ```
   mvn clean package
   sam build
   ```

3. Run the test script:
   ```
   ./test-next-best-action.sh
   ```
   This will invoke the Lambda function locally with two test events:
   - A basic test with default values
   - A detailed test with custom financial context data

4. Alternatively, you can invoke the function directly:
   ```
   sam local invoke NextBestActionFunction --event events/next-best-action-detailed-event.json
   ```

## API Usage

To use the Next Best Action API:

1. Send a POST request to the `/nextBestAction` endpoint with the following JSON body:

```json
{
  "userId": "user123",
  "maxRecommendations": 5,
  "financialContext": {
    "userId": "user123",
    "monthlyIncome": 7500.00,
    "totalSavings": 15000.00,
    "totalDebt": 45000.00,
    "creditScore": 680,
    "monthlyExpenses": 5000.00,
    "disposableIncome": 2500.00,
    "savingsRate": 0.10,
    "debtToIncomeRatio": 0.50,
    "accounts": [
      {
        "accountId": "ACC-1001",
        "accountType": "CHECKING",
        "accountName": "Primary Checking",
        "balance": 3500.00
      },
      {
        "accountId": "ACC-1002",
        "accountType": "SAVINGS",
        "accountName": "Emergency Fund",
        "balance": 11500.00,
        "interestRate": 0.01
      }
    ],
    "goals": [
      {
        "goalId": "GOAL-1001",
        "goalType": "EMERGENCY_FUND",
        "description": "Build emergency fund",
        "targetAmount": 20000.00,
        "currentAmount": 11500.00
      }
    ]
  },
  "userPreferences": {
    "userId": "user123",
    "riskTolerance": 3,
    "investmentHorizon": "MEDIUM_TERM",
    "preferredCategories": ["SAVINGS", "INVESTMENT"],
    "categoryPreferences": {
      "SAVINGS": 5,
      "INVESTMENT": 4
    }
  },
  "userBehaviors": [
    {
      "userId": "user123",
      "behaviorId": "VIEW_SAVINGS_ACCOUNT_1678901234",
      "actionType": "VIEW",
      "resourceType": "SAVINGS",
      "resourceId": "SAVINGS_ACCOUNT"
    }
  ]
}
```

2. The API will return a JSON response with personalized recommendations:

```json
{
  "userId": "user123",
  "recommendations": [
    {
      "actionType": "DEBT_MANAGEMENT",
      "description": "Reduce your debt-to-income ratio to improve financial health.",
      "confidenceScore": 0.75,
      "context": "Your current debt-to-income ratio is 50.0%, which is above the recommended 36% threshold.",
      "relatedProducts": [
        {
          "productId": "PROD-1001",
          "productName": "Debt Consolidation Loan",
          "productType": "LOAN",
          "description": "A debt consolidation loan to help you simplify payments and potentially reduce interest rates."
        }
      ]
    },
    {
      "actionType": "SAVINGS_RECOMMENDATION",
      "description": "Build an emergency fund covering at least 3-6 months of expenses.",
      "confidenceScore": 0.65,
      "context": "Your current savings would cover approximately 3.0 months of expenses, which is at the lower end of the recommended 3-6 month threshold.",
      "relatedProducts": [
        {
          "productId": "PROD-1002",
          "productName": "High-Yield Savings Account",
          "productType": "SAVINGS",
          "description": "A high-yield savings account with competitive interest rates and no monthly fees."
        }
      ]
    }
  ]
}
```

## Deployment

To deploy the Next Best Action feature to AWS:

1. Build the project:
   ```
   mvn clean package
   sam build
   ```

2. Deploy using SAM:
   ```
   sam deploy --guided
   ```

3. Follow the prompts to configure your deployment.

## Customization

You can customize the Next Best Action feature by:

1. Modifying the recommendation generation logic in `NextBestActionService.java`.
2. Adding new action types to the `ActionType` enum in `NextBestActionService.java`.
3. Enhancing the product search capabilities in `MockOpenSearchService.java`.
4. Implementing a real embedding service by updating `BedrockService.java` to use Amazon Bedrock or another embedding service.

## Limitations

The current implementation has the following limitations:

1. It uses mock implementations for the embedding and product search services.
2. The recommendation logic is based on predefined rules rather than machine learning models.
3. The product recommendations are generated using simple keyword matching rather than sophisticated semantic search.

These limitations can be addressed in future versions by integrating with real embedding services, implementing machine learning models, and enhancing the product search capabilities. 