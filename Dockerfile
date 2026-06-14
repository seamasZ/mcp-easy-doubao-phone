# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Final stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install adb for Android device communication
RUN apk add --no-cache android-tools

# Copy the built JAR from builder stage
COPY --from=builder /app/target/mcp-easy-doubao-phone-1.0.0-jar-with-dependencies.jar /app/mcp-easy-doubao-phone.jar

# Create non-root user for security
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser
USER appuser

# Set environment variables
ENV ADB_PATH=/usr/bin/adb

# Expose port if needed for future HTTP API
EXPOSE 8080

# Entry point
ENTRYPOINT ["java", "-jar", "/app/mcp-easy-doubao-phone.jar"]
