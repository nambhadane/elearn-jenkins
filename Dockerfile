# ================================================
# E-LearnHub Backend — Multi-Stage Docker Build
# ================================================
# Stage 1: Build the Spring Boot JAR
# ================================================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copy pom.xml first to leverage Docker layer caching for dependencies
COPY Backend/teacher-service/pom.xml ./

# Download all dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code and build the JAR (skip tests for faster builds)
COPY Backend/teacher-service/src ./src
RUN mvn clean package -DskipTests -B

# ================================================
# Stage 2: Run the application with a slim JRE
# ================================================
FROM eclipse-temurin:17-jre-alpine

# Add a non-root user for security best practice
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Create directories for file uploads
RUN mkdir -p /app/uploads/submissions /app/uploads/profiles /app/uploads/lessons \
    && chown -R appuser:appgroup /app

# Copy only the built JAR from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose the backend API port
EXPOSE 8082

# Health check — ensures Kubernetes knows when the container is ready
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8082/actuator/health || exit 1

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
