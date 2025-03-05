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
- **PersonalizationConversationalAgent**: Interactive agent providing personalized financial advice

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
- Providing conversational interfaces for personalized advice

## Running the Examples

The project includes several example applications:

1. **NextBestActionSimpleExample**: A simple example showing how to use the NextBestActionService
   ```
   mvn exec:java -Dexec.mainClass="com.enterprise.finance.personalization.NextBestActionSimpleExample"
   ```

2. **NextBestActionExamples**: More comprehensive examples with different user profiles
   ```
   mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="com.enterprise.finance.personalization.NextBestActionExamples"
   ```

3. **PersonalizationMetricsExample**: Demonstrates A/B testing of different personalization strategies
   ```
   mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="com.enterprise.finance.personalization.PersonalizationMetricsExample"
   ```

4. **ConversationalAgentExample**: Interactive example of the personalized financial advice agent
   ```
   mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="com.enterprise.finance.personalization.ConversationalAgentExample"
   ```

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

## Conversational Agent

The project includes a conversational agent that provides personalized financial advice through an interactive interface. The agent:

- Analyzes user profiles, behaviors, and financial context
- Provides personalized recommendations with behavioral nudges
- Tracks user satisfaction and financial impact
- Adjusts recommendations based on user feedback
- Incorporates insights from Vanguard's "Value of Personalized Advice" research

## Future Enhancements

See the [CONVERSATIONAL-AGENT-ENHANCEMENTS.md](CONVERSATIONAL-AGENT-ENHANCEMENTS.md) document for a detailed roadmap of planned enhancements, including:

1. **Natural Language Processing Integration**: Enabling more natural conversations
2. **Behavioral Finance Integration**: Addressing cognitive biases in financial decisions
3. **Life Event Prediction Engine**: Anticipating and planning for major life changes
4. **Dynamic Risk Profiling**: Adjusting risk assessments based on market conditions and life stages
5. **Multi-modal Interaction**: Supporting voice and visual interfaces
6. **Personalized Financial Education**: Delivering targeted learning based on knowledge gaps
7. **Social Proof and Community Insights**: Leveraging anonymized peer comparisons
8. **Integration with Financial Institutions**: Enabling seamless implementation of advice
9. **Emotional Intelligence**: Addressing the emotional aspects of financial decisions
10. **Longitudinal Outcome Tracking**: Measuring long-term impact of personalized advice

These enhancements aim to deliver the 3-4% improvement in financial outcomes demonstrated in Vanguard's research on personalized financial advice.