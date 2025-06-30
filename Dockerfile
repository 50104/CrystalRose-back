# Dockerfile
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY build/libs/back-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 4000
ENTRYPOINT ["java", "--enable-preview" ,"-jar", "app.jar"]
