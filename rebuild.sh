#!/bin/bash
echo "Cleaning and rebuilding the SnapCal Firebase Gemini app..."

# Make sure we're in the correct directory
cd "$(dirname "$0")"

# Clean the project
echo "Running Gradle clean..."
./gradlew clean

# Clear Android Studio caches
echo "Removing build directories..."
find . -type d -name "build" -exec rm -rf {} +
find . -type d -name ".gradle" -exec rm -rf {} +

# Rebuild the project
echo "Running Gradle build..."
./gradlew build

echo "Rebuild process completed."

# Instructions for running the app
echo ""
echo "To run the app:"
echo "1. Open Android Studio"
echo "2. Open this project"
echo "3. Click 'Run' to install on an emulator or device"
echo ""
echo "Alternatively, run './gradlew installDebug' to install on a connected device"
