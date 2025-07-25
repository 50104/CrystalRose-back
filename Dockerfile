# Dockerfile
FROM eclipse-temurin:17-jdk

WORKDIR /app

# JAR 복사
COPY build/libs/back-0.0.1-SNAPSHOT.jar app.jar

# 로그 폴더 + webhook 스크립트 복사
COPY discord-log.sh /app/discord-log.sh
RUN chmod +x /app/discord-log.sh && mkdir -p /app/logs

EXPOSE 4000

# app.jar 실행 + log 감시 스크립트 백그라운드 병렬 실행
ENTRYPOINT ["bash", "-c", "/app/discord-log.sh & java --enable-preview -jar app.jar"]
