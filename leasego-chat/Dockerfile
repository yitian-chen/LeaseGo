FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
COPY leasego-common/pom.xml leasego-common/
COPY leasego-model/pom.xml leasego-model/
COPY leasego-chat/pom.xml leasego-chat/
RUN mvn dependency:go-offline -B

COPY leasego-common/ leasego-common/
COPY leasego-model/ leasego-model/
COPY leasego-chat/ leasego-chat/
RUN mvn clean package -DskipTests -q -f leasego-chat/pom.xml

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/leasego-chat/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
