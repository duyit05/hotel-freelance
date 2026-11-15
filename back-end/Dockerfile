# Build image back-end:  docker build -t hotel-backend .
# ---- Build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
# Tải thư viện trước để lần build sau dùng lại cache nếu pom.xml không đổi
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- Run ----
FROM eclipse-temurin:21-jre-alpine
# Chạy bằng user thường, không dùng root
RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /app/uploads && chown app:app /app/uploads
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
USER app

ENV STORAGE_TYPE=local \
    UPLOAD_DIR=/app/uploads \
    JAVA_TOOL_OPTIONS="-Xmx512m -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
