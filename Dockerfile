# Build Rainbow jar
FROM gradle:8.5-alpine AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle build --no-daemon  # no daemon since building only once

# Launch Rainbow API
FROM openjdk:17-jdk-slim AS start

RUN mkdir /src
COPY --from=build /home/gradle/src/build/libs/rainbow-1.0.0.jar /src/rainbow-1.0.0.jar

ENTRYPOINT ["java","-jar","/src/rainbow-1.0.0.jar"]