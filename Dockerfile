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
LABEL name="starlite-api"\
      author="Derek Garcia" \
      github="dlg1206" \
      description="API service to navigate and search courses available at the University of Hawai'i"

RUN adduser -D starlite
WORKDIR /app
COPY --from=build --chown=starlite:starlite /build/build/libs/*.jar /app/starlite.jar

USER starlite
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/v2/campuses || exit 1

ENTRYPOINT ["java", "-jar", "starlite.jar"]