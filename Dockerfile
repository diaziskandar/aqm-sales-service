FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Perintah inilah yang otomatis menyalin file .jar dari komputer lokal ke dalam Docker
COPY target/salesservice-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]