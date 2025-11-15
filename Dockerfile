# ===========================
# Build stage
# ===========================
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

# Build the application and verify
RUN ./gradlew clean bootJar -x test --no-daemon --info && \
    echo "===== Build complete, checking output =====" && \
    ls -lh build/libs/ && \
    if [ ! -f build/libs/app.jar ]; then echo "ERROR: app.jar not found!"; exit 1; fi && \
    echo "===== app.jar verified ====="


# ===========================
# Runtime stage
# ===========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install Python, pip, ffmpeg, and create a virtual environment for yt-dlp
RUN apk add --no-cache python3 py3-pip ffmpeg && \
    python3 -m venv /opt/venv && \
    . /opt/venv/bin/activate && \
    pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir yt-dlp && \
    yt-dlp --version

# Add virtual environment binaries to PATH
ENV PATH="/opt/venv/bin:$PATH"

# Verify yt-dlp installation
RUN echo "===== Verifying yt-dlp installation =====" && \
    yt-dlp --version && \
    echo "===== yt-dlp installed successfully ====="

# Copy the JAR file from the build stage
COPY --from=build /build/build/libs/app.jar /app/app.jar

# Verify the JAR was copied
RUN ls -lh /app/app.jar && \
    echo "===== Runtime stage ready ====="

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "/app/app.jar"]
