#!/bin/bash

# Script to run all examples and tests for the Financial Services Personalization Platform

echo "===== Financial Services Personalization Platform Test Suite ====="
echo "This script will run all examples and tests to verify they work correctly."
echo ""

# Change to the project directory
cd FinancialServiceFunction

# Clean and compile the project
echo "===== Building the project ====="
mvn clean compile
if [ $? -ne 0 ]; then
    echo "Build failed. Exiting."
    exit 1
fi
echo ""

# Compile the test classes
echo "===== Compiling test classes ====="
mvn test-compile
if [ $? -ne 0 ]; then
    echo "Test compilation failed. Exiting."
    exit 1
fi
echo ""

# Run the PersonalizationMetricsExample
echo "===== Running PersonalizationMetricsExample ====="
echo "This example demonstrates A/B testing of different personalization strategies."
echo "It will simulate 100 users and generate metrics reports."
echo "Press Ctrl+C to skip this example if it takes too long."
echo ""
mvn exec:java -Dexec.classpathScope=test -Dexec.mainClass="com.enterprise.finance.personalization.PersonalizationMetricsExample"
echo ""

# Notify about the ConversationalAgentExample
echo "===== ConversationalAgentExample ====="
echo "The ConversationalAgentExample requires interactive user input."
echo "You can run it separately using:"
echo "  ./run-conversational-agent.sh"
echo ""

# Run unit tests
echo "===== Running unit tests ====="
mvn test
if [ $? -ne 0 ]; then
    echo "Unit tests failed. Exiting."
    exit 1
fi
echo ""

# Package the project
echo "===== Packaging the project ====="
mvn package
if [ $? -ne 0 ]; then
    echo "Packaging failed. Exiting."
    exit 1
fi
echo ""

echo "===== All tests completed successfully! ====="
echo "The Financial Services Personalization Platform is working correctly."
echo ""
echo "To run the conversational agent example, use:"
echo "  ./run-conversational-agent.sh"
echo "" 