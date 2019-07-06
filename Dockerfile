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
COPY etc/oauthproxy/oauth2_proxy /usr/oauth2_proxy

CMD java -jar /usr/skillwill.jar & /usr/oauth2_proxy --upstream "http://localhost:1337" --http-address "localhost:8080" --email-domain "torben.xyz" --cookie-secret "foobar" --client-id "175845741268-gn6j8jrlguee95o6qurdea7m2fmgmobi.apps.googleusercontent.com" --client-secret "mIjngOK5nH5I7bIEANdlmcMA"
