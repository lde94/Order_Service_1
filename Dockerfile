# === Stage 1: Build ===
FROM maven:3.9.9-amazoncorretto-21-debian AS build
WORKDIR /app

# Copy project files
COPY pom.xml ./
COPY src ./src

# ✅ Do NOT set secrets here
# Build args (optional, not sensitive) can be used for non-secret values
# ARG BUILD_ENV=prod

# Build jar (no secret exposure)
RUN mvn clean package -DskipTests


# === Stage 2: Runtime ===
FROM amazoncorretto:21-alpine
WORKDIR /app

# Optional: update tools
RUN apk update && apk upgrade --no-cache && apk add curl

# ✅ Only keep *default/fallbacks* that are safe for local runs
# These will be overridden by Azure environment variables at runtime
ENV JWT_EXPIRATION=3600000
ENV BACKEND_PORT=8080

# Copy built jar
COPY --from=build /app/target/*.jar app.jar

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Expose port
EXPOSE 8080

# Start the application
CMD ["java", "-jar", "/app/app.jar"]
