#!/bin/bash

# Create necessary directories
mkdir -p FinancialServiceFunction/target

# Compile the code
echo "Compiling the code..."
cd FinancialServiceFunction
javac -d target -cp ".:$(find ~/.m2/repository -name '*.jar' | tr '\n' ':')" $(find src -name "*.java") 2> compile_errors.txt

# Check for compilation errors
if [ $? -eq 0 ]; then
    echo "Compilation successful!"
else
    echo "Compilation failed. Here are the errors:"
    cat compile_errors.txt
fi 