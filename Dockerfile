# ==========================================================
# PENTAVA — multi-stage Dockerfile cho project multi-module Maven
# Build 1 lần cho cả reactor (common-starter + 4 service),
# sau đó mỗi service có 1 stage final riêng, nhẹ, chỉ chứa JRE + jar.
#
# Cách build từng service (chạy từ thư mục gốc PENTAVA, nơi có pom.xml cha):
#   docker build --target api-gateway       -t pentava/api-gateway:latest .
#   docker build --target auth-service      -t pentava/auth-service:latest .
#   docker build --target checkin-service   -t pentava/checkin-service:latest .
#   docker build --target onboarding-service -t pentava/onboarding-service:latest .
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

# Tải dependency trước (cache riêng bước này, đỡ tải lại khi chỉ đổi code)
RUN mvn -B -q dependency:go-offline || true

# Copy toàn bộ source thật
COPY common-starter ./common-starter
COPY api-gateway ./api-gateway
COPY auth-service ./auth-service
COPY checkin-service ./checkin-service
COPY onboarding-service ./onboarding-service

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
FROM eclipse-temurin:21-jre AS checkin-service
WORKDIR /app
COPY --from=build /workspace/checkin-service/target/checkin-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]

# ---------- STAGE 5: onboarding-service ----------
FROM eclipse-temurin:21-jre AS onboarding-service
WORKDIR /app
COPY --from=build /workspace/onboarding-service/target/onboarding-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]