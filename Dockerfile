# Dùng JDK nhỏ gọn
FROM openjdk:21-jdk-slim

# Copy file jar vào container
COPY target/email-0.0.1-SNAPSHOT.jar app.jar

# Chạy app
ENTRYPOINT ["java", "-jar", "/app.jar"]
