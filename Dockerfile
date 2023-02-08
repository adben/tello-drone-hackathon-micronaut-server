FROM  --platform=linux/amd64 openjdk:17-alpine
COPY target/tello-drone-hackathon-api-*.jar tello-drone-hackathon-api.jar
EXPOSE 8080
CMD ["java", "-jar", "tello-drone-hackathon-api.jar"]