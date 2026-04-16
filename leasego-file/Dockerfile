FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
COPY leasego-common/pom.xml leasego-common/
COPY leasego-model/pom.xml leasego-model/
COPY leasego-upms/pom.xml leasego-upms/
COPY leasego-gateway/pom.xml leasego-gateway/
COPY leasego-auth/pom.xml leasego-auth/
COPY leasego-region/pom.xml leasego-region/
COPY leasego-apartment/pom.xml leasego-apartment/
COPY leasego-appointment/pom.xml leasego-appointment/
COPY leasego-lease/pom.xml leasego-lease/
COPY leasego-chat/pom.xml leasego-chat/
COPY leasego-file/pom.xml leasego-file/
COPY leasego-sms/pom.xml leasego-sms/
RUN mvn dependency:go-offline -B

COPY leasego-common/ leasego-common/
COPY leasego-model/ leasego-model/
COPY leasego-upms/ leasego-upms/
COPY leasego-gateway/ leasego-gateway/
COPY leasego-auth/ leasego-auth/
COPY leasego-region/ leasego-region/
COPY leasego-apartment/ leasego-apartment/
COPY leasego-appointment/ leasego-appointment/
COPY leasego-lease/ leasego-lease/
COPY leasego-chat/ leasego-chat/
COPY leasego-file/ leasego-file/
COPY leasego-sms/ leasego-sms/
RUN mvn clean package -DskipTests -q -pl leasego-file -am

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/leasego-file/target/*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
