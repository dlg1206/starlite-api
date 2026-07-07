# Starlite API v2

> API that allows for searching for course details and generating schedules for the University of Hawai'i

## Quickstart Guide

### Gradle

```bash
gradle bootRun    # via Gradle
``` 

```bash
./gradlew bootRun # or via Gradle Wrapper
```

### Jar

1. Build jar

```bash
gradle bootJar    # via Gradle
```

```bash
./gradlew bootJar # or via Gradle Wrapper
```

2. Run jar

```bash
java -jar ./build/libs/starlite-2.0.0.jar
```

### Docker (Recommended)

1. Build image

```bash
docker build -t starlite-api:2.0.0 .
```

2. Run container

```bash
docker run --rm -p 8080:8080 starlite-api:2.0.0
```

## Usage

API service is now available at http://localhost:8080/api/v2

API endpoint documentation can be found at [API Endpoints](docs/endpoints.md#api-endpoints).

- [Get all Campuses](docs/endpoints.md#get-all-campuses)
- [Get all Terms](docs/endpoints.md#get-all-terms)
- [Get all Subjects](docs/endpoints.md#get-all-subjects)
- [Get all Courses (Single Subject)](docs/endpoints.md#get-all-courses-single-subject)
- [Filter Courses (Single Subject)](docs/endpoints.md#filter-courses-single-subject)
- [Get all Courses (Multiple Subjects)](docs/endpoints.md#get-all-courses-multiple-subjects)
- [Filter Courses (Multiple Subjects)](docs/endpoints.md#filter-courses-multiple-subjects)
- [Generate Schedules](docs/endpoints.md#generate-schedules)

Insomnia documentation is also available [here](docs/starlite-api-v2-docs.yaml).