# Starlite API v2

> API for searching for courses offered and generating schedules for all ten campuses at
> the [University of Hawai'i](https://www.hawaii.edu/)

## Quickstart Guide

### Docker (Recommended)

1. Build image

```bash
docker build -t starlite-api:2.2.0 .
```

2. Run container

```bash
docker run --rm -p 8080:8080 starlite-api:2.2.0
```

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
java -jar ./build/libs/starlite-2.2.0.jar
```

## Usage

API service is now available at http://localhost:8080/api/v2

API endpoint documentation can be found at [API Endpoints](docs/endpoints.md#api-endpoints).

**General Info**

- [Get all Campuses](docs/endpoints.md#get-all-campuses)
- [Get all Terms](docs/endpoints.md#get-all-terms)
- [Get all Subjects](docs/endpoints.md#get-all-subjects)
- [Get all Courses (Single Subject)](docs/endpoints.md#get-all-courses-single-subject)
- [Filter Courses (Single Subject)](docs/endpoints.md#filter-courses-single-subject)
- [Get all Courses (Multiple Subjects)](docs/endpoints.md#get-all-courses-multiple-subjects)
- [Filter Courses (Multiple Subjects)](docs/endpoints.md#filter-courses-multiple-subjects)

**Schedule Generation**

- [Generate Schedules](docs/endpoints.md#generate-schedules)
- [Decode Schedule to json](docs/endpoints.md#decode-schedule-to-json)
- [Decode Schedule to ics](docs/endpoints.md#decode-schedule-to-ics)

**Data Export**

- [Start Export Job](docs/endpoints.md#start-export-job)
- [Poll Export Job](docs/endpoints.md#poll-export-job)
- [Export endpoints](docs/endpoints.md#export-endpoints)
- [Export course data](docs/endpoints.md#export-course-data)

Insomnia documentation is also available [here](docs/starlite-api-v2-docs.yaml), make sure to change `Base Environment`
to the `Starlite` Environment in the
[environment dropdown menu](https://developer.konghq.com/insomnia/environments/#create-a-sub-environment) after
[importing](https://developer.konghq.com/insomnia/import-export/#import-methods).