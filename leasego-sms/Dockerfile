FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
COPY leasego-common/pom.xml leasego-common/
COPY leasego-model/pom.xml leasego-model/
COPY leasego-sms/pom.xml leasego-sms/
RUN mvn dependency:go-offline -B

COPY leasego-common/ leasego-common/
COPY leasego-model/ leasego-model/
COPY leasego-sms/ leasego-sms/
RUN mvn clean package -DskipTests -q -f leasego-sms/pom.xml

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/leasego-sms/target/*.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
