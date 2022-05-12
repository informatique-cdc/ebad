FROM openjdk:17-jdk-slim as Builder
WORKDIR source

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

RUN java -Djarmode=layertools -jar app.jar extract

FROM openjdk:17-slim

WORKDIR app

COPY --from=builder source/dependencies/ ./
COPY --from=builder source/spring-boot-loader/ ./
COPY --from=builder source/snapshot-dependencies/ ./
COPY --from=builder source/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
