# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /build

# Install dos2unix for handling line endings
RUN apk add --no-cache dos2unix

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Fix line endings and make gradlew executable
RUN dos2unix gradlew && chmod +x gradlew

# Copy source code
COPY src src

# Build the application
RUN ./gradlew clean build -x test --no-daemon

# Verify the JAR was created
RUN ls -la /build/build/libs/

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /build/build/libs/*.jar app.jar

# Verify the JAR was copied
RUN ls -la /app/

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "/app/app.jar"]
