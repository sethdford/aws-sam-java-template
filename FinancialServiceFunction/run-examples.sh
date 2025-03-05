#!/bin/bash

# Compile the code
echo "Compiling the code..."
mvn clean compile

# Run the examples
echo "Running the examples..."
mvn exec:java -Dexec.mainClass="com.enterprise.finance.personalization.NextBestActionExamples" 