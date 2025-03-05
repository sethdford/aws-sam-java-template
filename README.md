# AWS SAM Java Personalization

This project demonstrates a financial services personalization platform using AWS Bedrock and OpenSearch. It provides personalized financial product recommendations based on user preferences, behaviors, and financial context.

## Project Structure

The project is organized as follows:

- `FinancialServiceFunction/` - Contains the main Lambda function code
  - `src/main/java/com/enterprise/finance/personalization/` - Main source code
    - `model/` - Data models for financial products, user preferences, etc.
    - `service/` - Services for recommendation, embedding generation, and search
  - `src/test/java/` - Test code and examples

## Key Components

- **NextBestActionService**: Determines the next best actions for users based on their profile and behavior
- **BedrockService**: Generates embeddings for text using AWS Bedrock (currently mocked)
- **OpenSearchService**: Searches for similar products using vector embeddings (with mock implementation)

## Running the Examples

The project includes two example applications:

1. **NextBestActionSimpleExample**: A simple example showing how to use the NextBestActionService
   ```
   mvn exec:java -Dexec.mainClass="com.enterprise.finance.personalization.NextBestActionSimpleExample"
   ```

2. **NextBestActionExamples**: More comprehensive examples with different user profiles
   ```
   mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="com.enterprise.finance.personalization.NextBestActionExamples"
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
