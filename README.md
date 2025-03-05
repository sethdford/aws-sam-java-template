# AWS SAM Java Personalization

This project demonstrates a financial services personalization platform using AWS Bedrock and OpenSearch. It provides personalized financial product recommendations based on user preferences, behaviors, and financial context.

## Project Structure

The project is organized as follows:

- `FinancialServiceFunction/` - Contains the main Lambda function code
  - `src/main/java/com/enterprise/finance/personalization/` - Main source code
    - `model/` - Data models for financial products, user preferences, etc.
    - `service/` - Services for recommendation, embedding generation, and search
    - `metrics/` - Metrics tracking for personalization effectiveness
  - `src/test/java/` - Test code and examples

## Key Components

- **NextBestActionService**: Determines the next best actions for users based on their profile and behavior
- **BedrockService**: Generates embeddings for text using AWS Bedrock (currently mocked)
- **OpenSearchService**: Searches for similar products using vector embeddings (with mock implementation)
- **PersonalizationMetrics**: Tracks effectiveness metrics based on industry research
- **PersonalizationConversationalAgent**: Provides an interactive conversational interface for personalized advice

## Industry Research on Personalization

Financial services personalization has been shown to significantly improve customer outcomes and engagement. Industry research from leading firms like Vanguard highlights several key benefits:

- **Improved Financial Outcomes**: Personalized advice can lead to better financial decisions and improved portfolio performance
- **Higher Customer Satisfaction**: Tailored recommendations increase customer satisfaction and loyalty
- **Increased Engagement**: Personalized experiences drive higher engagement with financial services
- **Better Risk Management**: Customized advice helps clients maintain appropriate risk levels based on their unique situation

This project implements personalization best practices by:
- Considering user preferences and risk tolerance
- Analyzing past behavior patterns
- Accounting for financial context and life events
- Using AI-driven similarity matching for recommendations
- Measuring effectiveness through comprehensive metrics

## Running the Examples

The project includes several example applications and test scripts:

### Using the Test Scripts

1. **Run All Tests**: To run all examples and tests (except interactive ones):
   ```
   ./run-all-tests.sh
   ```
   This script will:
   - Build and compile the project
   - Run the PersonalizationMetricsExample
   - Run all unit tests
   - Package the project

2. **Run Conversational Agent**: To run the interactive conversational agent:
   ```
   ./run-conversational-agent.sh
   ```
   This script will start an interactive session with the conversational agent using a sample user profile.

### Running Individual Examples

1. **NextBestActionSimpleExample**: A simple example showing how to use the NextBestActionService
   ```
   cd FinancialServiceFunction
   mvn exec:java -Dexec.mainClass="com.enterprise.finance.personalization.NextBestActionSimpleExample"
   ```

2. **NextBestActionExamples**: More comprehensive examples with different user profiles
   ```
   cd FinancialServiceFunction
   mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="com.enterprise.finance.personalization.NextBestActionExamples"
   ```

3. **PersonalizationMetricsExample**: Demonstrates A/B testing of different personalization strategies
   ```
   cd FinancialServiceFunction
   mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="com.enterprise.finance.personalization.PersonalizationMetricsExample"
   ```

4. **ConversationalAgentExample**: Interactive example of the conversational agent
   ```
   cd FinancialServiceFunction
   mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="com.enterprise.finance.personalization.ConversationalAgentExample"
   ```

## Documentation

The project includes comprehensive documentation:

- **README.md**: This file, providing an overview of the project
- **PERSONALIZATION-APPROACH.md**: Details on the personalization approach and industry research
- **RECOMMENDED-ENHANCEMENTS.md**: Recommended future enhancements for the platform
- **CONVERSATIONAL-AGENT-EXPLANATION.md**: Detailed explanation of the conversational agent implementation
- **SUMMARY.md**: Summary of the project's features and accomplishments

## Recent Fixes

The following issues were fixed in the codebase:

1. **MockOpenSearchService**:
   - Fixed return type mismatches between `List<FinancialProduct>` and `List<Product>`
   - Added a helper method to convert between product types
   - Removed unnecessary `throws IOException` declarations

2. **OpenSearchService**:
   - Added default endpoint handling when environment variables are not set
   - Added a constructor that accepts an endpoint parameter

3. **BedrockService**:
   - Added a `generateEmbeddings` method to handle multiple texts

4. **NextBestActionSimpleExample**:
   - Fixed method calls to match the actual model classes

5. **Test Files**:
   - Fixed type mismatches in PersonalizationMetricsExample
   - Corrected date conversion issues in test data generation

## Dependencies

The project uses the following key dependencies:

- AWS SDK for Java v2
- AWS Bedrock Runtime
- OpenSearch Client
- AWS Lambda Powertools
- Jackson for JSON processing

## Building and Deploying

To build the project:

```
mvn clean package
```

To deploy with AWS SAM:

```
sam deploy --guided
```

## Measuring Personalization Effectiveness

To measure the effectiveness of personalization, this project includes a comprehensive metrics tracking system:

1. **Recommendation Relevance**: Tracks how often users engage with recommended products
2. **Financial Impact**: Measures projected financial improvements from following recommendations
3. **User Satisfaction**: Collects feedback metrics on recommendation quality
4. **A/B Testing Framework**: Compares different personalization strategies:
   - Baseline Strategy: Standard risk-based recommendations
   - Goal-Prioritized Strategy: Recommendations prioritized by financial goal importance
   - Behavior-Driven Strategy: Recommendations influenced by past user behavior patterns
   - Hybrid Approach: Combined strategy weighing all factors

These metrics align with industry research showing that effective personalization can improve financial outcomes by helping users make better decisions aligned with their goals.