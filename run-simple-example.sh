#!/bin/bash

# Compile the code
echo "Compiling the code..."
cd FinancialServiceFunction
mvn clean compile

# Check for compilation errors
if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    
    # Run the simple example
    echo "Running the simple example..."
    mvn exec:java -Dexec.mainClass="com.enterprise.finance.personalization.NextBestActionSimpleExample"
else
    echo "Compilation failed. Please check the error messages above."
fi 