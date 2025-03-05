#!/bin/bash

# Make sure the events directory exists
mkdir -p events

# Check if SAM CLI is installed
if ! command -v sam &> /dev/null
then
    echo "SAM CLI is not installed. Please install it first."
    echo "Visit: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html"
    exit 1
fi

# Run the basic test
echo "Running basic test with default values..."
sam local invoke NextBestActionFunction --event events/next-best-action-event.json

# Run the detailed test
echo -e "\n\nRunning detailed test with custom financial context..."
sam local invoke NextBestActionFunction --event events/next-best-action-detailed-event.json

echo -e "\n\nTests completed!" 