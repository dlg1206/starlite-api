FROM gradle:9.6.0-jdk21-alpine AS build
WORKDIR /build
# cache deps
COPY --chown=gradle:gradle build.gradle settings.gradle /build/
RUN gradle build --no-daemon -x test || return 0
# build
COPY --chown=gradle:gradle src/ /build/src/
RUN gradle build --no-daemon

FROM eclipse-temurin:21-alpine AS runtime
ENV API_VERSION=2.0.0
LABEL name="rainbow-api"\
      author="Derek Garcia" \
      github="dlg1206" \
      description="API service to navigate and search courses available at the University of Hawai'i"

RUN adduser -D rainbow
WORKDIR /rainbow
COPY --from=build --chown=rainbow:rainbow /build/build/libs/*.jar /rainbow/rainbow.jar

USER rainbow
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "rainbow.jar"]