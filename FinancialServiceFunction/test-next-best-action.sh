#!/bin/bash

# Compile the code
echo "Compiling the code..."
mvn clean package

# Create a test event
cat > test-event.json << EOF
{
  "body": "{\"userId\":\"user123\",\"userPreferences\":{\"userId\":\"user123\",\"riskTolerance\":\"CONSERVATIVE\",\"investmentHorizon\":\"SHORT_TERM\",\"preferredCategories\":[\"FIXED_INCOME\",\"DIVIDEND\"],\"categoryPreferences\":{\"FIXED_INCOME\":0.7,\"DIVIDEND\":0.3}},\"financialContext\":{\"userId\":\"user123\",\"annualIncome\":75000,\"totalAssets\":250000,\"totalLiabilities\":50000,\"goals\":[{\"goalId\":\"goal1\",\"type\":\"RETIREMENT\",\"description\":\"Retirement at 65\",\"targetAmount\":1000000,\"currentAmount\":150000,\"startDate\":\"2018-01-01T00:00:00Z\",\"targetDate\":\"2043-01-01T00:00:00Z\",\"priority\":\"HIGH\"}]}}"
}
EOF

# Run the Lambda handler locally
echo "Running the Lambda handler locally..."
java -cp target/personalization-1.0-SNAPSHOT.jar com.enterprise.finance.personalization.NextBestActionHandler test-event.json

echo "Done!" 