FROM openjdk:11.0

RUN addgroup -S ebad && adduser -S ebad -G ebad
USER ebad:ebad

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
