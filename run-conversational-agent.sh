#!/bin/bash

# Script to run the Personalization Conversational Agent Example

echo "Building the project..."
cd FinancialServiceFunction
mvn clean compile

echo "Running the Conversational Agent Example..."
mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="com.enterprise.finance.personalization.ConversationalAgentExample"

echo "Done!" 