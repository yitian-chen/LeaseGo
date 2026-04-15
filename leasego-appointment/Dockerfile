FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
COPY leasego-common/pom.xml leasego-common/
COPY leasego-model/pom.xml leasego-model/
COPY leasego-appointment/pom.xml leasego-appointment/
RUN mvn dependency:go-offline -B

COPY leasego-common/ leasego-common/
COPY leasego-model/ leasego-model/
COPY leasego-appointment/ leasego-appointment/
RUN mvn clean package -DskipTests -q -f leasego-appointment/pom.xml

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/leasego-appointment/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
