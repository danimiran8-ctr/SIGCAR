FROM openjdk:21
COPY ./target/SIGCAR-1.jar app.jar
EXPOSE 8213
ENTRYPOINT ["java", "-jar", "app.jar"]
