# Dùng JDK nhỏ gọn
FROM openjdk:21-jdk-slim

# Copy file jar vào container
COPY target/email-0.0.1-SNAPSHOT.jar app.jar

# Chạy app
ENTRYPOINT ["java", "-jar", "/app.jar"]FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/email-0.0.1-SNAPSHOT.jar app.jar

RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["sh", "-c", "echo '📡 Waiting for MySQL...'; \
  until nc -z mysql 3306; do echo '⌛ Waiting for MySQL...'; sleep 3; done; \
  echo '✅ Starting Spring Boot app'; \
  exec java -jar /app/app.jar"]

