# Build stage
FROM gradle:7.6-jdk17 AS build

WORKDIR /app

# Copy build configuration files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Build application (skipping ktlint and tests as requested)
RUN gradle bootJar -x ktlintCheck -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Application configuration
VOLUME /app/config

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
