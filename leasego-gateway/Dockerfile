FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 复制依赖和源码
COPY pom.xml .
COPY leasego-common/pom.xml leasego-common/
COPY leasego-model/pom.xml leasego-model/
COPY leasego-gateway/pom.xml leasego-gateway/
RUN mvn dependency:go-offline -B

COPY leasego-common/ leasego-common/
COPY leasego-model/ leasego-model/
COPY leasego-gateway/ leasego-gateway/
RUN mvn clean package -DskipTests -q -f leasego-gateway/pom.xml

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/leasego-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
