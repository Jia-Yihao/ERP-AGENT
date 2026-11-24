# gateway 服务镜像：maven 多阶段构建 + Spring Boot 分层 jar 运行
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -q -DskipTests -pl gateway -am package

FROM eclipse-temurin:17-jre
ENV LANG=C.UTF-8 \
    TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m"
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd -r app && useradd -r -g app -d /app app
WORKDIR /app
COPY --from=build /workspace/gateway/target/erp-gateway-1.0.0.jar /tmp/app.jar
RUN java -Djarmode=layertools -jar /tmp/app.jar extract --destination /app \
 && rm /tmp/app.jar \
 && chown -R app:app /app
USER app
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -cp /app/dependencies/:/app/spring-boot-loader/:/app/snapshot-dependencies/:/app/application/ org.springframework.boot.loader.launch.JarLauncher"]
