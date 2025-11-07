# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Make gradlew executable (Linux format)
RUN chmod +x gradlew && dos2unix gradlew 2>/dev/null || sed -i 's/\r$//' gradlew

# Build the application
RUN ./gradlew clean build -x test --no-daemon

# List the built files for debugging
RUN ls -la build/libs/

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar from build stage - use wildcard to find the jar
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
