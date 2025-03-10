FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# JAR 파일 복사 - 로컬에서 이미 빌드된 JAR 파일을 사용
COPY build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
