#!/bin/bash

# Parse version from properties
VERSION="1.0.0"

# Build jar
echo "Building jar. . ."
./gradlew -q clean bootJar

# Launch jar
echo "Done! Launching api. . ."
java -jar ./build/libs/rainbow-${VERSION}.jar