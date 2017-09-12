FROM openjdk:8-jdk as builder
COPY . /project
WORKDIR /project
RUN ./gradlew build -x test

FROM openjdk:8-jre-alpine
COPY --from=builder /project/build/libs/*.jar /fms.jar
RUN mkdir -p /var/log/openbaton
COPY --from=builder /project/etc/fms.properties /etc/openbaton/fms.properties
ENTRYPOINT ["java", "-jar", "/fms.jar", "--spring.config.location=file:/etc/openbaton/fms.properties"]
EXPOSE 9000
