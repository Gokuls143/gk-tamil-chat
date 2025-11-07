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

# Build the application (bootJar produces only the executable jar)
RUN ./gradlew clean bootJar -x test --no-daemon --stacktrace || (echo "BUILD FAILED" && exit 1)

# ===== DEBUGGING: Check if JAR file exists =====
RUN echo "===== Checking build directory =====" && \
    ls -laR /build/build/libs/ && \
    echo "===== Finding all JAR files =====" && \
    find /build -name "*.jar" -type f && \
    echo "===== Verifying app.jar exists and is not empty =====" && \
    test -f /build/build/libs/app.jar && echo "app.jar found" || (echo "ERROR: app.jar NOT FOUND" && exit 1) && \
    ls -lh /build/build/libs/app.jar && \
    echo "===== End of JAR search ====="

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the specific executable JAR file from build stage
COPY --from=build /build/build/libs/app.jar app.jar

# ===== DEBUGGING: Verify JAR was copied to runtime stage =====
RUN echo "===== Checking /app directory =====" && \
    ls -la /app/ && \
    echo "===== Checking if app.jar exists =====" && \
    test -f /app/app.jar && echo "app.jar EXISTS" || echo "app.jar DOES NOT EXIST" && \
    echo "===== File details =====" && \
    file /app/app.jar 2>/dev/null || echo "Cannot read app.jar"

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "/app/app.jar"]
