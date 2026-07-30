FROM openjdk:21-jdk

ADD /build/libs/*.jar app.jar

USER nobody

ENTRYPOINT ["java", "-jar", "app.jar"]