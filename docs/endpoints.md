# API Endpoints

> Insomnia documentation is also available [here](rainbow-api-v1-docs.yaml)

- [Get all Campuses](#get-all-campuses)
- [Get all Terms](#get-all-terms)
- [Get all Subjects](#get-all-subjects)
- [Get all Courses (Single Subject)](#get-all-courses-single-subject)
- [Filter Courses (Single Subject)](#filter-courses-single-subject)
- [Get all Courses (Multiple Subjects)](#get-all-courses-multiple-subjects)
- [Filter Courses (Multiple Subjects)](#filter-courses-multiple-subjects)
- [Generate Schedules](#generate-schedules)

## Get All Campuses

> Get list of the University of Hawai'i campus codes

**Endpoint:** `http://localhost:8080/api/v2/campuses`

**Request Method:** `GET`

### Responses

| Response Code | Type               | Description                                          |
|:-------------:|--------------------|------------------------------------------------------|
|      200      | IdentifierResponse | List of University of Hawai'i campus codes and names |

### Example

```bash
curl http://localhost:8080/api/v2/campuses
```

## Get All Terms

> Get all terms available for a University of Hawai'i campus

**Endpoint:** `http://localhost:8080/api/v2/campuses/{campusCode}/terms`

**Request Method:** `GET`

### Path Variables

|  Variable  | Type   | Description                         |
|:----------:|--------|-------------------------------------|
| campusCode | String | UH Campus code to get the terms for |

### Responses

| Response Code | Type                       | Description                          |
|:-------------:|----------------------------|--------------------------------------|
|      200      | IdentifierResponse         | List of term codes and names         |
|      400      | InvalidCampusCodeException | Requested campus code does not exist |

### Example

```bash
# Get all terms for the University of Hawai'i at Mānoa
curl http://localhost:8080/api/v2/campuses/man/terms
```

## Get All Subjects

> Get all subjects for a University of Hawai'i campus and term

**Endpoint:** `http://localhost:8080/api/v2/campuses/{campusCode}/terms/{termCode}/subjects`

**Request Method:** `GET`

### Path Variables

|  Variable  | Type   | Description                            |
|:----------:|--------|----------------------------------------|
| campusCode | String | UH campus code to get the subjects for |
|  termCode  | String | Term code to get the subjects for      |

### Responses

| Response Code | Type                       | Description                                            |
|:-------------:|----------------------------|--------------------------------------------------------|
|      200      | IdentifierResponse         | List of subject names and IDs                          |
|      400      | InvalidCampusCodeException | Requested campus code does not exist                   |
|      400      | InvalidTermCodeException   | Requested term code does exist for the provided campus |

### Example

```bash
# Get all subjects offered at the University of Hawai'i at Mānoa for Fall 2026
curl http://localhost:8080/api/v2/campuses/man/terms/202710/subjects
```

## Get All Courses (Single Subject)

> Get all courses for a single subject for a University of Hawai'i campus and term

**Endpoint:** `http://localhost:8080/api/v2/campuses/{campusCode}/terms/{termCode}/subjects/{subjectCode}`

**Request Method:** `GET`

### Path Variables

|  Variable   | Type   | Description                            |
|:-----------:|--------|----------------------------------------|
| campusCode  | String | UH campus code to get the subjects for |
|  termCode   | String | Term code to get the subjects for      |
| subjectCode | String | Subject code to get the courses for    |

### Query Params

| Variable | Type    | Description                            | Default | Required? |
|:--------:|---------|----------------------------------------|:-------:|:---------:|
| detailed | Boolean | Return section details with the course |  False  |    ❌️     |

### Responses

| Response Code | Type                         | Description                                                        |
|:-------------:|------------------------------|--------------------------------------------------------------------|
|      200      | IdentifierResponse           | List of courses and their details                                  |
|      400      | InvalidCampusCodeException   | Requested campus code does not exist                               |
|      400      | InvalidTermCodeException     | Requested term code does exist for the provided campus             |
|      400      | InvalidSubjectCodesException | Requested subject code does exist for the provided campus and term |

### Example

```bash
# Get all ICS courses offered at the University of Hawai'i at Mānoa for Fall 2026
curl http://localhost:8080/api/v2/campuses/man/terms/202710/subjects/ics
```

## Filter Courses (Single Subject)

> Get all courses for a single subject for a University of Hawai'i campus and term

**Endpoint:** `http://localhost:8080/api/v2/campuses/{campusCode}/terms/{termCode}/subjects/{subjectCode}`

**Request Method:** `POST`

### Path Variables

|  Variable   | Type   | Description                            |
|:-----------:|--------|----------------------------------------|
| campusCode  | String | UH campus code to get the subjects for |
|  termCode   | String | Term code to get the subjects for      |
| subjectCode | String | Subject code to get the courses for    |

### Query Params

| Variable | Type    | Description                            | Default | Required? |
|:--------:|---------|----------------------------------------|:-------:|:---------:|
| detailed | Boolean | Return section details with the course |  False  |    ❌️     |

### Request Body

| Field                 | Type      | Description                                                                                          |
|-----------------------|-----------|------------------------------------------------------------------------------------------------------|
| accept_crns           | Integer[] | Course reference numbers to exclusively include                                                      |
| reject_crns           | Integer[] | Course reference numbers to exclusively exclude                                                      |
| accept_course_numbers | String[]  | Course numbers to exclusively include. <br> `*` wild card can be used ie `1**`-> 101, 102, 110 etc.  |
| reject_course_numbers | String[]  | Course numbers to exclusively exclude. <br> `*` wild card can be used ie `1**` -> 101, 102, 110 etc. |
| accept_days           | Day[]     | Days of the week to exclusively include                                                              |
| reject_days           | Day[]     | Days of the week exclusively exclude                                                                 |
| start_after           | String    | Earliest time a class can start in HH:mm format (24hr)                                               |
| end_before            | String    | Latest time a class can run in HH:mm format (24hr)                                                   |
| only_online           | Boolean   | Whether to include or exclude exclusively online classes                                             |
| only_async            | Boolean   | Whether to include or exclude exclusively online sync classes                                        |
| has_major_restriction | Boolean   | Whether to include or exclude exclusively classes with major restrictions                            |
| has_prereq            | Boolean   | Whether to include or exclude exclusively classes with prerequisites                                 |
| can_audit             | Boolean   | Whether to include or exclude exclusively classes with an audit option                               |
| exclude_full          | Boolean   | Whether to include or exclude exclusively completely full classes                                    |
| exclude_waitlisted    | Boolean   | Whether to include or exclude exclusively completely classes with a waitlist                         |
| accept_instructors    | String[]  | Instructor usernames to exclusively allow                                                            |
| reject_instructors    | String[]  | Instructor usernames to reject                                                                       |
| accept_title_keywords | String[]  | Keywords in course name to exclusively accept                                                        |
| reject_title_keywords | String[]  | Keywords in course name to exclusively reject                                                        |
| accept_desc_keywords  | String[]  | Keywords in course description to exclusively accept                                                 |
| reject_desc_keywords  | String[]  | Keywords in course description to exclusively reject                                                 |

`Day` are either the first letters (`thu`), full day (`thursday`), or UH day code (`R`)

### Responses

| Response Code | Type                         | Description                                                        |
|:-------------:|------------------------------|--------------------------------------------------------------------|
|      200      | CourseResponse               | List of courses and their details                                  |
|      400      | InvalidCampusCodeException   | Requested campus code does not exist                               |
|      400      | InvalidTermCodeException     | Requested term code does exist for the provided campus             |
|      400      | InvalidSubjectCodesException | Requested subject code does exist for the provided campus and term |
|      400      | InvalidRequestBodyResponse   | Request body is invalid                                            |

### Examples

```bash
# Get ICS 101, 211, and any 300 level course offered at the University of Hawai'i at Mānoa for Fall 2026
curl --request POST \
  --url http://localhost:8080/api/v2/campuses/man/terms/202710/subjects/ics \
  --header 'Content-Type: application/json' \
  --data '{
	"accept_course_numbers": [
		"101", "211", "3**"
	]
}'
```

```bash
# Get all sections for ICS offered at the University of Hawai'i at Mānoa for Fall 2026 that aren't on Monday and starts after 10:00 am
curl --request POST \
  --url http://localhost:8080/api/v2/campuses/man/terms/202710/subjects/ics \
  --header 'Content-Type: application/json' \
  --data '{
	"reject_days": [
		"m"
	],
	"start_after": "10:00"
}'
```

## Get All Courses (Multiple Subjects)

> Get all courses for multiple subjects for a University of Hawai'i campus and term

**Endpoint:** `http://localhost:8080/api/v2/campuses/{campusCode}/terms/{termCode}/courses`

**Request Method:** `GET`

### Path Variables

|  Variable   | Type   | Description                            |
|:-----------:|--------|----------------------------------------|
| campusCode  | String | UH campus code to get the subjects for |
|  termCode   | String | Term code to get the subjects for      |
| subjectCode | String | Subject code to get the courses for    |

### Query Params

| Variable | Type     | Description                            | Default | Required? |
|:--------:|----------|----------------------------------------|:-------:|:---------:|
| subjects | String[] | Subject codes of courses to fetch      |         |    ✔️     |
| detailed | Boolean  | Return section details with the course |  False  |    ❌️     |

> [!WARNING]
> Fetching all subjects can be expensive!
>
> It is highly recommended to request no more than 3 courses at a time, but can do so if needed

### Responses

| Response Code | Type                         | Description                                                        |
|:-------------:|------------------------------|--------------------------------------------------------------------|
|      200      | CourseResponse               | List of courses and their details                                  |
|      400      | InvalidCampusCodeException   | Requested campus code does not exist                               |
|      400      | InvalidTermCodeException     | Requested term code does exist for the provided campus             |
|      400      | InvalidSubjectCodesException | Requested subject code does exist for the provided campus and term |

### Example

```bash
# Get all ICS and ECE courses offered at the University of Hawai'i at Mānoa for Fall 2026
curl http://localhost:8080/api/v2/campuses/man/terms/202710/subjects/icss?subjects=ics,ece
```

## Filter Courses (Multiple Subjects)

> Get all courses for multiple subjects for a University of Hawai'i campus and term

**Endpoint:** `http://localhost:8080/api/v2/campuses/{campusCode}/terms/{termCode}/courses`

**Request Method:** `POST`

### Path Variables

|  Variable   | Type   | Description                            |
|:-----------:|--------|----------------------------------------|
| campusCode  | String | UH campus code to get the subjects for |
|  termCode   | String | Term code to get the subjects for      |
| subjectCode | String | Subject code to get the courses for    |

### Query Params

| Variable | Type     | Description                            | Default | Required? |
|:--------:|----------|----------------------------------------|:-------:|:---------:|
| subjects | String[] | Subject codes of courses to fetch      |         |    ✔️     |
| detailed | Boolean  | Return section details with the course |  False  |    ❌️     |

> [!WARNING]
> Fetching all subjects can be expensive!
>
> It is highly recommended to request no more than 3 courses at a time, but can do so if needed

### Request Body

| Field                 | Type      | Description                                                                                                      |
|-----------------------|-----------|------------------------------------------------------------------------------------------------------------------|
| accept_crns           | Integer[] | Course reference numbers to exclusively include                                                                  |
| reject_crns           | Integer[] | Course reference numbers to exclusively exclude                                                                  |
| accept_course_numbers | String[]  | Course numbers to exclusively include. <br> `*` wild card can be used ie `1**`-> 101, 102, 110 etc.              |
| reject_course_numbers | String[]  | Course numbers to exclusively exclude. <br> `*` wild card can be used ie `1**` -> 101, 102, 110 etc.             |
| accept_course_ids     | String[]  | Course IDs to exclusively include. <br> `*` wild card can be used ie `ICS 1**` -> ICS 101, ICS 102, ICS 110 etc. |
| reject_course_ids     | String[]  | Course IDs to exclusively exclude. <br> `*` wild card can be used ie `ICS 1**` -> ICS 101, ICS 102, ICS 110 etc. |
| accept_days           | Day[]     | Days of the week to exclusively include                                                                          |
| reject_days           | Day[]     | Days of the week exclusively exclude                                                                             |
| start_after           | String    | Earliest time a class can start in HH:mm format (24hr)                                                           |
| end_before            | String    | Latest time a class can run in HH:mm format (24hr)                                                               |
| only_online           | Boolean   | Whether to include or exclude exclusively online classes                                                         |
| only_async            | Boolean   | Whether to include or exclude exclusively online sync classes                                                    |
| has_major_restriction | Boolean   | Whether to include or exclude exclusively classes with major restrictions                                        |
| has_prereq            | Boolean   | Whether to include or exclude exclusively classes with prerequisites                                             |
| can_audit             | Boolean   | Whether to include or exclude exclusively classes with an audit option                                           |
| exclude_full          | Boolean   | Whether to include or exclude exclusively completely full classes                                                |
| exclude_waitlisted    | Boolean   | Whether to include or exclude exclusively completely classes with a waitlist                                     |
| accept_instructors    | String[]  | Instructor usernames to exclusively allow                                                                        |
| reject_instructors    | String[]  | Instructor usernames to reject                                                                                   |
| accept_title_keywords | String[]  | Keywords in course name to exclusively accept                                                                    |
| reject_title_keywords | String[]  | Keywords in course name to exclusively reject                                                                    |
| accept_desc_keywords  | String[]  | Keywords in course description to exclusively accept                                                             |
| reject_desc_keywords  | String[]  | Keywords in course description to exclusively reject                                                             |

`Day` are either the first letters (`thu`), full day (`thursday`), or UH day code (`R`)

### Responses

| Response Code | Type                         | Description                                                        |
|:-------------:|------------------------------|--------------------------------------------------------------------|
|      200      | CourseResponse               | List of courses and their details                                  |
|      400      | InvalidCampusCodeException   | Requested campus code does not exist                               |
|      400      | InvalidTermCodeException     | Requested term code does exist for the provided campus             |
|      400      | InvalidSubjectCodesException | Requested subject code does exist for the provided campus and term |
|      400      | InvalidRequestBodyResponse   | Request body is invalid                                            |

### Example

```bash
# Get ICS 101, 211, and any 300 level ICS or ECE course offered at the University of Hawai'i at Mānoa for Fall 2026
curl --request POST \
  --url http://localhost:8080/api/v2/campuses/man/terms/202710/courses?subjects=ics,ece \
  --header 'Content-Type: application/json' \
  --data '{
	"accept_course_id": [
		"101", "211", "3**"
	],
	"accept_course_numbers":[
		"3**"
	]
}'
```

```bash
# Get all sections for ICS or ECE offered at the University of Hawai'i at Mānoa for Fall 2026 that aren't on Monday and starts after 10:00 am
curl --request POST \
  --url http://localhost:8080/api/v2/campuses/man/terms/202710/courses?subjects=ics,ece \
  --header 'Content-Type: application/json' \
  --data '{
	"reject_days": [
		"m"
	],
	"start_after": "10:00"
}'
```

## Generate Schedules

> Filter courses and sections to generate all possible schedules

**Endpoint:** `http://localhost:8080/api/v2/campuses/{campusCode}/terms/{termCode}/schedule`

**Request Method:** `POST`

### Path Variables

|  Variable  | Type   | Description                          |
|:----------:|--------|--------------------------------------|
| campusCode | String | UH Campus ID to get the sections for |
|  termCode  | String | Term ID to get the sections for      |

### Request Body

| Field       | Type              | Description                                                      |  Default   | Required? |
|-------------|-------------------|------------------------------------------------------------------|:----------:|:---------:|
| courses     | RequestedCourse[] | List of requested courses to make a schedule with                |            |    ✔️     |
| buffer_time | Integer           | Minimum time allowed between classes and blocks                  | No minimum |    ❌️     |
| blocks      | Block[]           | List of reserved blocks of time that no classes can be scheduled |    None    |    ❌️     |

_RequestedCourse_

| Field        | Type      | Description                                                                                  | Default  | Required? |
|--------------|-----------|----------------------------------------------------------------------------------------------|:--------:|:---------:|
| subject_code | String    | Subject code of course                                                                       |          |    ✔️     |
| number       | String    | Number of course <br> (wildcards are NOT supported)                                          |          |    ✔️     |
| crns         | Integer[] | List of course reference numbers that belong to that course that can be used in the schedule | All CRNs |    ❌️     |

_Block_

| Field | Type   | Description                                |   Default    | Required? |
|-------|--------|--------------------------------------------|:------------:|:---------:|
| start | String | Start time of block in HH:mm format (24hr) |              |    ✔️     |
| end   | String | End time of block in HH:mm format (24hr)   |              |    ✔️     |
| crns  | Day[]  | List of days this block occurs on          | All weekdays |    ❌️     |

`Day` are either the first letters (`thu`), full day (`thursday`), or UH day code (`R`)

### Responses

| Response Code | Type                                   | Description                                                        |
|:-------------:|----------------------------------------|--------------------------------------------------------------------|
|      200      | ScheduleResponse                       | List of schedules and their details                                |
|      400      | InvalidCampusCodeException             | Requested campus code does not exist                               |
|      400      | InvalidTermCodeException               | Requested term code does exist for the provided campus             |
|      400      | InvalidSubjectCodesException           | Requested subject code does exist for the provided campus and term |
|      400      | InvalidRequestBodyResponse             | Request body is invalid                                            |
|      400      | InvalidCourseIDsException              | Course number uses a wildcard or number not found for subject      |
|      400      | InvalidCourseReferenceNumbersException | Course reference number not found for course                       |
|      400      | InvalidTimeSpansException              | Block time starts after it ends                                    |

### Examples

```bash
# Generate all schedules for ICS 101, ICS 111, and ICS 141 offered at 
# the University of Hawai'i at Mānoa for Fall 2026
curl --request POST \
  --url http://localhost:8080/api/v2/campuses/man/terms/202710/schedule \
  --header 'Content-Type: application/json' \
  --data '{
	"courses": [
		{
			"subject_code": "ICS",
			"number": "101"
		},
				{
			"subject_code": "ICS",
			"number": "111"
		},
				{
			"subject_code": "ICS",
			"number": "141"
		}
	]
}'
```

```bash
# Generate all schedules for ICS 101 (75018, 75019) and ECE 11323L that
# allow for a minimum of 30 minutes between classes and have no class 
# between 9 and 12 on wednesdays and fridays offered at the University
# of Hawai'i at Mānoa for Fall 2026
curl --request POST \
  --url http://localhost:8080/api/v2/campuses/man/terms/202710/schedule \
  --header 'Content-Type: application/json' \
  --data '{
  "buffer_time": 30,
  "courses": [
    {
      "subject_code": "ICS",
      "number": "101",
      "crns": [
        75018,
        75019
      ]
    },
    {
      "subject_code": "ECE",
      "number": "323L"
    }
  ],
  "blocks": [
    {
      "day": [
        "wed",
        "fri"
      ],
      "start": "09:00",
      "end": "12:00"
    }
  ]
}'
```
