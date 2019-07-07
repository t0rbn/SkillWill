# Stage 1
FROM maven:3.6.1-jdk-12 as maven

WORKDIR /usr/src/app
COPY pom.xml pom.xml
COPY src src

RUN mvn clean install

# Stage 2
FROM openjdk:12.0.1-jdk
EXPOSE 8080
COPY --from=maven /usr/src/app/target/skillwill.jar /usr/skillwill.jar
CMD java -jar /usr/skillwill.jar
