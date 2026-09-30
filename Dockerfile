# ==========================================================
# PENTAVA — multi-stage Dockerfile cho project multi-module Maven
# Build 1 lần cho cả reactor (common-starter + 5 service),
# sau đó mỗi service có 1 stage final riêng, nhẹ, chỉ chứa JRE + jar.
#
# Cách build từng service (chạy từ thư mục gốc PENTAVA, nơi có pom.xml cha):
#   docker build --target api-gateway       -t pentava/api-gateway:latest .
#   docker build --target auth-service      -t pentava/auth-service:latest .
#   docker build --target checkin-service   -t pentava/checkin-service:latest .
#   docker build --target onboarding-service -t pentava/onboarding-service:latest .
#   docker build --target cinema-service    -t pentava/cinema-service:latest .
#
# (docker-compose.yml đi kèm sẽ tự làm việc này cho bạn, không cần build tay)
# ==========================================================

# ---------- STAGE 1: build toàn bộ reactor bằng Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy pom cha trước để tận dụng Docker layer cache khi chỉ sửa code
COPY pom.xml .
COPY common-starter/pom.xml common-starter/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY checkin-service/pom.xml checkin-service/pom.xml
COPY onboarding-service/pom.xml onboarding-service/pom.xml
COPY cinema-service/pom.xml cinema-service/pom.xml
COPY social-service/pom.xml social-service/pom.xml
COPY shop-service/pom.xml shop-service/pom.xml
COPY skin-service/pom.xml skin-service/pom.xml
COPY manage-service/pom.xml manage-service/pom.xml

# Tải dependency trước (cache riêng bước này, đỡ tải lại khi chỉ đổi code)
RUN mvn -B -q dependency:go-offline || true

# Copy toàn bộ source thật
COPY common-starter ./common-starter
COPY api-gateway ./api-gateway
COPY auth-service ./auth-service
COPY checkin-service ./checkin-service
COPY onboarding-service ./onboarding-service
COPY cinema-service ./cinema-service
COPY social-service ./social-service
COPY shop-service ./shop-service
COPY skin-service ./skin-service
COPY manage-service ./manage-service

# Build toàn bộ reactor (common-starter sẽ được install vào .m2 local
# trong image build này để các service khác dùng được)
RUN mvn -B clean install -DskipTests

# ---------- STAGE 2: api-gateway ----------
FROM eclipse-temurin:21-jre AS api-gateway
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# ---------- STAGE 3: auth-service ----------
# Service duy nhất có gRPC server (port 9090), các service khác gọi vào đây qua gRPC
FROM eclipse-temurin:21-jre AS auth-service
WORKDIR /app
COPY --from=build /workspace/auth-service/target/auth-service-*.jar app.jar
EXPOSE 8082
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]

# ---------- STAGE 4: checkin-service ----------
# gRPC server (port 9091) cho cinema-service gọi vào, chỉ dùng trong mạng Docker
FROM eclipse-temurin:21-jre AS checkin-service
WORKDIR /app
COPY --from=build /workspace/checkin-service/target/checkin-service-*.jar app.jar
EXPOSE 8084
EXPOSE 9091
ENTRYPOINT ["java", "-jar", "app.jar"]

# ---------- STAGE 5: onboarding-service ----------
# gRPC server (port 9092) cho cinema-service gọi vào lấy danh sách mục tiêu, chỉ dùng trong mạng Docker
FROM eclipse-temurin:21-jre AS onboarding-service
WORKDIR /app
COPY --from=build /workspace/onboarding-service/target/onboarding-service-*.jar app.jar
EXPOSE 8083
EXPOSE 9092
ENTRYPOINT ["java", "-jar", "app.jar"]

# ---------- STAGE 6: cinema-service ----------
FROM eclipse-temurin:21-jre AS cinema-service
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg fonts-noto \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/cinema-service/target/cinema-service-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]

# ---------- STAGE 7: social-service ----------
FROM eclipse-temurin:21-jre AS social-service
WORKDIR /app
COPY --from=build /workspace/social-service/target/social-service-*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]

# ---------- STAGE 8: shop-service ----------
FROM eclipse-temurin:21-jre AS shop-service
WORKDIR /app
COPY --from=build /workspace/shop-service/target/shop-service-*.jar app.jar
EXPOSE 8087
EXPOSE 9094
ENTRYPOINT ["java", "-jar", "app.jar"]

# ---------- STAGE 9: skin-service ----------
FROM eclipse-temurin:21-jre AS skin-service
WORKDIR /app
COPY --from=build /workspace/skin-service/target/skin-service-*.jar app.jar
EXPOSE 8088
EXPOSE 9095
ENTRYPOINT ["java", "-jar", "app.jar"]

# ---------- STAGE 10: manage-service ----------
FROM eclipse-temurin:21-jre AS manage-service
WORKDIR /app
COPY --from=build /workspace/manage-service/target/manage-service-*.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]