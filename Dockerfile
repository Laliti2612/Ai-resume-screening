FROM maven:3.9-eclipse-temurin-17-alpine
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/resume-screening-0.0.1-SNAPSHOT.jar"]