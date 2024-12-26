#
# Build Rainbow API jar
#
FROM gradle:8.12.0-jdk21-alpine AS build

WORKDIR /home/gradle
COPY --chown=gradle:gradle build.gradle src/ /home/gradle/src/
COPY --chown=gradle:gradle build.gradle settings.gradle /home/gradle/

RUN gradle build --no-daemon  # no daemon since building only once

#
# Launch Rainbow API
#
FROM eclipse-temurin:21-alpine AS runtime

ARG API_NAME=rainbow-api
ARG API_VERSION=1.1.0

LABEL name=$API_NAME \
      version=$API_VERSION \
      author="Derek Garcia" \
      description="API service to navigate and search courses available at the University of Hawaii"

RUN adduser -D rainbow

WORKDIR /rainbow
COPY --from=build --chown=rainbow:rainbow /home/gradle/build/libs/$API_NAME-$API_VERSION.jar /rainbow/rainbow.jar

USER rainbow
ENTRYPOINT ["java","-jar","rainbow.jar"]