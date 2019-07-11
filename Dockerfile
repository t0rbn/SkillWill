# Build
FROM maven:3.6.1-jdk-12 as maven

WORKDIR /usr/src/app
COPY pom.xml pom.xml
COPY src src

RUN mvn clean install

# Run
FROM openjdk:12.0.1-jdk
ENV PORT $PORT
ENV MONGOURI $MONGOURI
COPY --from=maven /usr/src/app/target/skillwill.jar /usr/skillwill.jar
CMD java -jar -Dspring.data.mongodb.uri=${MONGOURI} /usr/skillwill.jar --server.port=${PORT}
