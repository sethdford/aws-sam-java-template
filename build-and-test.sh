#!/bin/bash

# Create necessary directories
mkdir -p FinancialServiceFunction/src/main/java/com/enterprise/finance/personalization/model
mkdir -p FinancialServiceFunction/src/main/java/com/enterprise/finance/personalization/service
mkdir -p FinancialServiceFunction/src/test/java/com/enterprise/finance/personalization

# Compile the code
echo "Compiling the code..."
cd FinancialServiceFunction
mvn clean compile

# Check for compilation errors
if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    
    # Run the examples
    echo "Running the examples..."
    mvn exec:java -Dexec.mainClass="com.enterprise.finance.personalization.NextBestActionExamples"
else
    echo "Compilation failed. Please check the error messages above."
fi 