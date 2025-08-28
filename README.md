# Course Service API Documentation

This document provides details about all the API endpoints available in the Course Service. The frontend developer can use this as a reference to connect to the backend.

## Base URL

All endpoints are prefixed with `/api`

## Authentication

Most endpoints accept an optional `userId` parameter to personalize results. Where applicable, include the user's ID to get user-specific data.

## Courses

### Get All Courses

Returns a list of all available courses.

- **URL**: `/courses`
- **Method**: `GET`
- **Query Parameters**:
  - `userId` (optional): User ID to check enrollment status
- **Example Request**:
  ```
  GET /api/courses?userId=user123
  ```
- **Example Response**:
  ```json
  [
    {
      "id": 1,
      "title": "Introduction to Spring Boot",
      "description": "Learn the basics of Spring Boot",
      "instructorId": "instructor123",
      "instructorName": "John Doe",
      "category": "Programming",
      "createdAt": "2025-08-27T00:12:12.292221",
      "moduleCount": 2,
      "enrollmentCount": 10,
      "completionPercentage": 0.0,
      "modules": null,
      "userEnrolled": false
    },
    {
      "id": 2,
      "title": "Advanced Java Programming",
      "description": "Master Java programming techniques",
      "instructorId": "instructor456",
      "instructorName": "Jane Smith",
      "category": "Programming",
      "createdAt": "2025-08-26T15:30:45.123456",
      "moduleCount": 5,
      "enrollmentCount": 25,
      "completionPercentage": 0.0,
      "modules": null,
      "userEnrolled": true
    }
  ]
  ```

### Get Course by ID

Returns details of a specific course.

- **URL**: `/courses/{id}`
- **Method**: `GET`
- **URL Parameters**:
  - `id`: Course ID
- **Query Parameters**:
  - `userId` (optional): User ID to check enrollment status
  - `includeModules` (default: false): Whether to include module details
- **Example Request**:
  ```
  GET /api/courses/1?userId=user123&includeModules=true
  ```
- **Example Response**:
  ```json
  {
    "id": 1,
    "title": "Introduction to Spring Boot",
    "description": "Learn the basics of Spring Boot",
    "instructorId": "instructor123",
    "instructorName": "John Doe",
    "category": "Programming",
    "createdAt": "2025-08-27T00:12:12.292221",
    "moduleCount": 2,
    "enrollmentCount": 10,
    "completionPercentage": 25.0,
    "modules": [
      {
        "id": 1,
        "title": "Getting Started",
        "type": "VIDEO",
        "contentUrl": "https://storage.example.com/videos/intro.mp4",
        "moduleOrder": 1,
        "completed": false,
        "courseId": 1,
        "courseTitle": "Introduction to Spring Boot"
      },
      {
        "id": 2,
        "title": "Basic Concepts",
        "type": "PDF",
        "contentUrl": "https://storage.example.com/pdfs/concepts.pdf",
        "moduleOrder": 2,
        "completed": true,
        "courseId": 1,
        "courseTitle": "Introduction to Spring Boot"
      }
    ],
    "userEnrolled": true
  }
  ```

### Get Courses by Instructor ID

Returns all courses taught by a specific instructor.

- **URL**: `/courses/instructor/{instructorId}`
- **Method**: `GET`
- **URL Parameters**:
  - `instructorId`: Instructor ID
- **Query Parameters**:
  - `userId` (optional): User ID to check enrollment status
- **Example Request**:
  ```
  GET /api/courses/instructor/instructor123?userId=user123
  ```
- **Example Response**: Same format as Get All Courses

### Get Courses by Category

Returns all courses in a specific category.

- **URL**: `/courses/category/{category}`
- **Method**: `GET`
- **URL Parameters**:
  - `category`: Course category
- **Query Parameters**:
  - `userId` (optional): User ID to check enrollment status
- **Example Request**:
  ```
  GET /api/courses/category/Programming?userId=user123
  ```
- **Example Response**: Same format as Get All Courses

### Get Enrolled Courses

Returns all courses a user is enrolled in.

- **URL**: `/courses/enrolled`
- **Method**: `GET`
- **Query Parameters**:
  - `userId` (required): User ID
- **Example Request**:
  ```
  GET /api/courses/enrolled?userId=user123
  ```
- **Example Response**: Same format as Get All Courses

### Search Courses

Search for courses by title or description.

- **URL**: `/courses/search`
- **Method**: `GET`
- **Query Parameters**:
  - `query` (required): Search query
  - `userId` (optional): User ID to check enrollment status
- **Example Request**:
  ```
  GET /api/courses/search?query=spring&userId=user123
  ```
- **Example Response**: Same format as Get All Courses

### Create Course

Creates a new course.

- **URL**: `/courses`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "title": "New Course Title",
    "description": "Course description",
    "instructorId": "instructor123",
    "category": "Programming"
  }
  ```
- **Example Response**:
  ```json
  {
    "id": 3,
    "title": "New Course Title",
    "description": "Course description",
    "instructorId": "instructor123",
    "instructorName": null,
    "category": "Programming",
    "createdAt": "2025-08-28T12:34:56.789012",
    "moduleCount": 0,
    "enrollmentCount": 0,
    "completionPercentage": 0.0,
    "modules": null,
    "userEnrolled": false
  }
  ```

### Update Course

Updates an existing course.

- **URL**: `/courses/{id}`
- **Method**: `PUT`
- **URL Parameters**:
  - `id`: Course ID
- **Request Body**:
  ```json
  {
    "title": "Updated Course Title",
    "description": "Updated course description",
    "instructorId": "instructor123",
    "category": "Programming"
  }
  ```
- **Example Response**: Same format as Create Course

### Delete Course

Deletes a course.

- **URL**: `/courses/{id}`
- **Method**: `DELETE`
- **URL Parameters**:
  - `id`: Course ID
- **Example Request**:
  ```
  DELETE /api/courses/3
  ```
- **Example Response**: 204 No Content

## Modules

### Get All Modules

Returns a list of all modules.

- **URL**: `/modules`
- **Method**: `GET`
- **Example Request**:
  ```
  GET /api/modules
  ```
- **Example Response**:
  ```json
  [
    {
      "id": 1,
      "title": "Getting Started",
      "type": "VIDEO",
      "contentUrl": "https://storage.example.com/videos/intro.mp4",
      "moduleOrder": 1,
      "completed": false,
      "courseId": 1,
      "courseTitle": "Introduction to Spring Boot"
    },
    {
      "id": 2,
      "title": "Basic Concepts",
      "type": "PDF",
      "contentUrl": "https://storage.example.com/pdfs/concepts.pdf",
      "moduleOrder": 2,
      "completed": false,
      "courseId": 1,
      "courseTitle": "Introduction to Spring Boot"
    }
  ]
  ```

### Get Module by ID

Returns details of a specific module.

- **URL**: `/modules/{id}`
- **Method**: `GET`
- **URL Parameters**:
  - `id`: Module ID
- **Query Parameters**:
  - `userId` (optional): User ID to check completion status
- **Example Request**:
  ```
  GET /api/modules/1?userId=user123
  ```
- **Example Response**:
  ```json
  {
    "id": 1,
    "title": "Getting Started",
    "type": "VIDEO",
    "contentUrl": "https://storage.example.com/videos/intro.mp4",
    "moduleOrder": 1,
    "completed": true,
    "courseId": 1,
    "courseTitle": "Introduction to Spring Boot",
    "quiz": {
      "id": 1,
      "title": "Getting Started Quiz",
      "moduleId": 1,
      "questions": [
        {
          "id": 1,
          "questionText": "What is Spring Boot?",
          "quizId": 1,
          "answers": [
            {
              "id": 1,
              "answerText": "A Java framework",
              "isCorrect": true,
              "questionId": 1
            },
            {
              "id": 2,
              "answerText": "A JavaScript library",
              "isCorrect": false,
              "questionId": 1
            }
          ]
        }
      ]
    }
  }
  ```

### Get Modules by Course ID

Returns all modules for a specific course.

- **URL**: `/modules/course/{courseId}`
- **Method**: `GET`
- **URL Parameters**:
  - `courseId`: Course ID
- **Query Parameters**:
  - `userId` (optional): User ID to check completion status
- **Example Request**:
  ```
  GET /api/modules/course/1?userId=user123
  ```
- **Example Response**: Array of module objects as in Get All Modules

### Get Modules by Type

Returns all modules of a specific type.

- **URL**: `/modules/type/{type}`
- **Method**: `GET`
- **URL Parameters**:
  - `type`: Module type (VIDEO, PDF, QUIZ, LIVE_SESSION)
- **Query Parameters**:
  - `userId` (optional): User ID to check completion status
- **Example Request**:
  ```
  GET /api/modules/type/VIDEO?userId=user123
  ```
- **Example Response**: Array of module objects as in Get All Modules

### Get Available Modules

Returns modules available to a user based on their coins.

- **URL**: `/modules/course/{courseId}/available`
- **Method**: `GET`
- **URL Parameters**:
  - `courseId`: Course ID
- **Query Parameters**:
  - `userCoins` (required): Number of coins the user has
  - `userId` (optional): User ID to check completion status
- **Example Request**:
  ```
  GET /api/modules/course/1/available?userCoins=100&userId=user123
  ```
- **Example Response**: Array of module objects as in Get All Modules

### Create Module

Creates a new module.

- **URL**: `/modules`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "title": "New Module",
    "type": "VIDEO",
    "contentUrl": "https://storage.example.com/videos/new.mp4",
    "moduleOrder": 3,
    "courseId": 1
  }
  ```
- **Example Response**:
  ```json
  {
    "id": 3,
    "title": "New Module",
    "type": "VIDEO",
    "contentUrl": "https://storage.example.com/videos/new.mp4",
    "moduleOrder": 3,
    "completed": false,
    "courseId": 1,
    "courseTitle": "Introduction to Spring Boot"
  }
  ```

### Update Module

Updates an existing module.

- **URL**: `/modules/{id}`
- **Method**: `PUT`
- **URL Parameters**:
  - `id`: Module ID
- **Request Body**:
  ```json
  {
    "title": "Updated Module",
    "type": "VIDEO",
    "contentUrl": "https://storage.example.com/videos/updated.mp4",
    "moduleOrder": 3,
    "courseId": 1
  }
  ```
- **Example Response**: Same format as Create Module

### Delete Module

Deletes a module.

- **URL**: `/modules/{id}`
- **Method**: `DELETE`
- **URL Parameters**:
  - `id`: Module ID
- **Example Request**:
  ```
  DELETE /api/modules/3
  ```
- **Example Response**: 204 No Content

### Reorder Module

Changes the order of a module within a course.

- **URL**: `/modules/{id}/reorder`
- **Method**: `POST`
- **URL Parameters**:
  - `id`: Module ID
- **Query Parameters**:
  - `newOrder` (required): New position in the course
- **Example Request**:
  ```
  POST /api/modules/3/reorder?newOrder=1
  ```
- **Example Response**: 200 OK

## Enrollments

### Get All Enrollments

Returns a list of all enrollments.

- **URL**: `/enrollments`
- **Method**: `GET`
- **Example Request**:
  ```
  GET /api/enrollments
  ```
- **Example Response**:
  ```json
  [
    {
      "id": 1,
      "userId": "user123",
      "courseId": 1,
      "courseTitle": "Introduction to Spring Boot",
      "enrolledAt": "2025-08-25T10:15:30.123456"
    },
    {
      "id": 2,
      "userId": "user456",
      "courseId": 2,
      "courseTitle": "Advanced Java Programming",
      "enrolledAt": "2025-08-26T14:20:45.678901"
    }
  ]
  ```

### Get Enrollment by ID

Returns details of a specific enrollment.

- **URL**: `/enrollments/{id}`
- **Method**: `GET`
- **URL Parameters**:
  - `id`: Enrollment ID
- **Example Request**:
  ```
  GET /api/enrollments/1
  ```
- **Example Response**:
  ```json
  {
    "id": 1,
    "userId": "user123",
    "courseId": 1,
    "courseTitle": "Introduction to Spring Boot",
    "enrolledAt": "2025-08-25T10:15:30.123456"
  }
  ```

### Get Enrollments by User ID

Returns all enrollments for a specific user.

- **URL**: `/enrollments/user/{userId}`
- **Method**: `GET`
- **URL Parameters**:
  - `userId`: User ID
- **Example Request**:
  ```
  GET /api/enrollments/user/user123
  ```
- **Example Response**: Array of enrollment objects as in Get All Enrollments

### Get Enrollments by Course ID

Returns all enrollments for a specific course.

- **URL**: `/enrollments/course/{courseId}`
- **Method**: `GET`
- **URL Parameters**:
  - `courseId`: Course ID
- **Example Request**:
  ```
  GET /api/enrollments/course/1
  ```
- **Example Response**: Array of enrollment objects as in Get All Enrollments

### Check Enrollment

Checks if a user is enrolled in a course.

- **URL**: `/enrollments/check`
- **Method**: `GET`
- **Query Parameters**:
  - `userId` (required): User ID
  - `courseId` (required): Course ID
- **Example Request**:
  ```
  GET /api/enrollments/check?userId=user123&courseId=1
  ```
- **Example Response**:
  ```json
  true
  ```

### Create Enrollment

Enrolls a user in a course.

- **URL**: `/enrollments`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "userId": "user123",
    "courseId": 1
  }
  ```
- **Example Response**:
  ```json
  {
    "id": 3,
    "userId": "user123",
    "courseId": 1,
    "courseTitle": "Introduction to Spring Boot",
    "enrolledAt": "2025-08-28T12:34:56.789012"
  }
  ```

### Delete Enrollment

Deletes an enrollment.

- **URL**: `/enrollments/{id}`
- **Method**: `DELETE`
- **URL Parameters**:
  - `id`: Enrollment ID
- **Example Request**:
  ```
  DELETE /api/enrollments/3
  ```
- **Example Response**: 204 No Content

### Unenroll User from Course

Unenrolls a user from a course.

- **URL**: `/enrollments/user/{userId}/course/{courseId}`
- **Method**: `DELETE`
- **URL Parameters**:
  - `userId`: User ID
  - `courseId`: Course ID
- **Example Request**:
  ```
  DELETE /api/enrollments/user/user123/course/1
  ```
- **Example Response**: 204 No Content

## Progress

### Get All Progress

Returns a list of all progress records.

- **URL**: `/progress`
- **Method**: `GET`
- **Example Request**:
  ```
  GET /api/progress
  ```
- **Example Response**:
  ```json
  [
    {
      "id": 1,
      "userId": "user123",
      "moduleId": 1,
      "moduleTitle": "Getting Started",
      "courseId": 1,
      "courseTitle": "Introduction to Spring Boot",
      "completed": true,
      "completedAt": "2025-08-27T15:30:45.123456"
    },
    {
      "id": 2,
      "userId": "user123",
      "moduleId": 2,
      "moduleTitle": "Basic Concepts",
      "courseId": 1,
      "courseTitle": "Introduction to Spring Boot",
      "completed": false,
      "completedAt": null
    }
  ]
  ```

### Get Progress by ID

Returns details of a specific progress record.

- **URL**: `/progress/{id}`
- **Method**: `GET`
- **URL Parameters**:
  - `id`: Progress ID
- **Example Request**:
  ```
  GET /api/progress/1
  ```
- **Example Response**:
  ```json
  {
    "id": 1,
    "userId": "user123",
    "moduleId": 1,
    "moduleTitle": "Getting Started",
    "courseId": 1,
    "courseTitle": "Introduction to Spring Boot",
    "completed": true,
    "completedAt": "2025-08-27T15:30:45.123456"
  }
  ```

### Get Progress by User ID

Returns all progress records for a specific user.

- **URL**: `/progress/user/{userId}`
- **Method**: `GET`
- **URL Parameters**:
  - `userId`: User ID
- **Example Request**:
  ```
  GET /api/progress/user/user123
  ```
- **Example Response**: Array of progress objects as in Get All Progress

### Get Progress by Module ID

Returns all progress records for a specific module.

- **URL**: `/progress/module/{moduleId}`
- **Method**: `GET`
- **URL Parameters**:
  - `moduleId`: Module ID
- **Example Request**:
  ```
  GET /api/progress/module/1
  ```
- **Example Response**: Array of progress objects as in Get All Progress

### Get Progress by User ID and Module ID

Returns progress for a specific user on a specific module.

- **URL**: `/progress/user/{userId}/module/{moduleId}`
- **Method**: `GET`
- **URL Parameters**:
  - `userId`: User ID
  - `moduleId`: Module ID
- **Example Request**:
  ```
  GET /api/progress/user/user123/module/1
  ```
- **Example Response**: Progress object as in Get Progress by ID

### Get Progress by User ID and Course ID

Returns all progress records for a specific user on a specific course.

- **URL**: `/progress/user/{userId}/course/{courseId}`
- **Method**: `GET`
- **URL Parameters**:
  - `userId`: User ID
  - `courseId`: Course ID
- **Example Request**:
  ```
  GET /api/progress/user/user123/course/1
  ```
- **Example Response**: Array of progress objects as in Get All Progress

### Get Course Progress Stats

Returns progress statistics for a user on a course.

- **URL**: `/progress/user/{userId}/course/{courseId}/stats`
- **Method**: `GET`
- **URL Parameters**:
  - `userId`: User ID
  - `courseId`: Course ID
- **Example Request**:
  ```
  GET /api/progress/user/user123/course/1/stats
  ```
- **Example Response**:
  ```json
  {
    "totalModules": 5,
    "completedModules": 2,
    "completionPercentage": 40.0,
    "startedAt": "2025-08-25T10:15:30.123456",
    "lastActivityAt": "2025-08-27T15:30:45.123456"
  }
  ```

### Mark Module as Completed

Marks a module as completed for a user.

- **URL**: `/progress/user/{userId}/module/{moduleId}/complete`
- **Method**: `POST`
- **URL Parameters**:
  - `userId`: User ID
  - `moduleId`: Module ID
- **Example Request**:
  ```
  POST /api/progress/user/user123/module/2/complete
  ```
- **Example Response**:
  ```json
  {
    "id": 2,
    "userId": "user123",
    "moduleId": 2,
    "moduleTitle": "Basic Concepts",
    "courseId": 1,
    "courseTitle": "Introduction to Spring Boot",
    "completed": true,
    "completedAt": "2025-08-28T12:34:56.789012"
  }
  ```

### Reset Module Progress

Resets progress for a user on a module.

- **URL**: `/progress/user/{userId}/module/{moduleId}/reset`
- **Method**: `POST`
- **URL Parameters**:
  - `userId`: User ID
  - `moduleId`: Module ID
- **Example Request**:
  ```
  POST /api/progress/user/user123/module/1/reset
  ```
- **Example Response**:
  ```json
  {
    "id": 1,
    "userId": "user123",
    "moduleId": 1,
    "moduleTitle": "Getting Started",
    "courseId": 1,
    "courseTitle": "Introduction to Spring Boot",
    "completed": false,
    "completedAt": null
  }
  ```

### Delete Progress

Deletes a progress record.

- **URL**: `/progress/{id}`
- **Method**: `DELETE`
- **URL Parameters**:
  - `id`: Progress ID
- **Example Request**:
  ```
  DELETE /api/progress/1
  ```
- **Example Response**: 204 No Content

## Quizzes

### Get All Quizzes

Returns a list of all quizzes.

- **URL**: `/quizzes`
- **Method**: `GET`
- **Example Request**:
  ```
  GET /api/quizzes
  ```
- **Example Response**:
  ```json
  [
    {
      "id": 1,
      "title": "Spring Boot Basics Quiz",
      "moduleId": 1,
      "questions": [
        {
          "id": 1,
          "questionText": "What is Spring Boot?",
          "quizId": 1,
          "answers": [
            {
              "id": 1,
              "answerText": "A Java framework",
              "isCorrect": true,
              "questionId": 1
            },
            {
              "id": 2,
              "answerText": "A JavaScript library",
              "isCorrect": false,
              "questionId": 1
            }
          ]
        }
      ]
    }
  ]
  ```

### Get Quiz by ID

Returns details of a specific quiz.

- **URL**: `/quizzes/{id}`
- **Method**: `GET`
- **URL Parameters**:
  - `id`: Quiz ID
- **Example Request**:
  ```
  GET /api/quizzes/1
  ```
- **Example Response**:
  ```json
  {
    "id": 1,
    "title": "Spring Boot Basics Quiz",
    "moduleId": 1,
    "questions": [
      {
        "id": 1,
        "questionText": "What is Spring Boot?",
        "quizId": 1,
        "answers": [
          {
            "id": 1,
            "answerText": "A Java framework",
            "isCorrect": true,
            "questionId": 1
          },
          {
            "id": 2,
            "answerText": "A JavaScript library",
            "isCorrect": false,
            "questionId": 1
          }
        ]
      }
    ]
  }
  ```

### Get Quizzes by Module ID

Returns all quizzes for a specific module.

- **URL**: `/quizzes/module/{moduleId}`
- **Method**: `GET`
- **URL Parameters**:
  - `moduleId`: Module ID
- **Example Request**:
  ```
  GET /api/quizzes/module/1
  ```
- **Example Response**: Array of quiz objects as in Get All Quizzes

### Create Quiz

Creates a new quiz.

- **URL**: `/quizzes`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "title": "New Quiz",
    "moduleId": 1,
    "questions": [
      {
        "questionText": "What is Spring?",
        "answers": [
          {
            "answerText": "A Java framework",
            "isCorrect": true
          },
          {
            "answerText": "A season",
            "isCorrect": false
          }
        ]
      }
    ]
  }
  ```
- **Example Response**:
  ```json
  {
    "id": 2,
    "title": "New Quiz",
    "moduleId": 1,
    "questions": [
      {
        "id": 2,
        "questionText": "What is Spring?",
        "quizId": 2,
        "answers": [
          {
            "id": 3,
            "answerText": "A Java framework",
            "isCorrect": true,
            "questionId": 2
          },
          {
            "id": 4,
            "answerText": "A season",
            "isCorrect": false,
            "questionId": 2
          }
        ]
      }
    ]
  }
  ```

### Update Quiz

Updates an existing quiz.

- **URL**: `/quizzes/{id}`
- **Method**: `PUT`
- **URL Parameters**:
  - `id`: Quiz ID
- **Request Body**:
  ```json
  {
    "title": "Updated Quiz",
    "moduleId": 1,
    "questions": [
      {
        "id": 2,
        "questionText": "Updated question",
        "answers": [
          {
            "id": 3,
            "answerText": "Updated answer",
            "isCorrect": true
          },
          {
            "id": 4,
            "answerText": "Another answer",
            "isCorrect": false
          }
        ]
      }
    ]
  }
  ```
- **Example Response**: Same format as Create Quiz

### Delete Quiz

Deletes a quiz.

- **URL**: `/quizzes/{id}`
- **Method**: `DELETE`
- **URL Parameters**:
  - `id`: Quiz ID
- **Example Request**:
  ```
  DELETE /api/quizzes/2
  ```
- **Example Response**: 204 No Content

## Quiz Questions

### Get All Quiz Questions

Returns a list of all quiz questions.

- **URL**: `/quiz-questions`
- **Method**: `GET`
- **Example Request**:
  ```
  GET /api/quiz-questions
  ```
- **Example Response**:
  ```json
  [
    {
      "id": 1,
      "questionText": "What is Spring Boot?",
      "quizId": 1,
      "answers": [
        {
          "id": 1,
          "answerText": "A Java framework",
          "isCorrect": true,
          "questionId": 1
        },
        {
          "id": 2,
          "answerText": "A JavaScript library",
          "isCorrect": false,
          "questionId": 1
        }
      ]
    }
  ]
  ```

### Get Quiz Question by ID

Returns details of a specific quiz question.

- **URL**: `/quiz-questions/{id}`
- **Method**: `GET`
- **URL Parameters**:
  - `id`: Question ID
- **Example Request**:
  ```
  GET /api/quiz-questions/1
  ```
- **Example Response**:
  ```json
  {
    "id": 1,
    "questionText": "What is Spring Boot?",
    "quizId": 1,
    "answers": [
      {
        "id": 1,
        "answerText": "A Java framework",
        "isCorrect": true,
        "questionId": 1
      },
      {
        "id": 2,
        "answerText": "A JavaScript library",
        "isCorrect": false,
        "questionId": 1
      }
    ]
  }
  ```

### Get Quiz Questions by Quiz ID

Returns all questions for a specific quiz.

- **URL**: `/quiz-questions/quiz/{quizId}`
- **Method**: `GET`
- **URL Parameters**:
  - `quizId`: Quiz ID
- **Example Request**:
  ```
  GET /api/quiz-questions/quiz/1
  ```
- **Example Response**: Array of question objects as in Get All Quiz Questions

### Create Quiz Question

Creates a new quiz question.

- **URL**: `/quiz-questions`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "questionText": "What is JPA?",
    "quizId": 1,
    "answers": [
      {
        "answerText": "Java Persistence API",
        "isCorrect": true
      },
      {
        "answerText": "Java Programming Application",
        "isCorrect": false
      }
    ]
  }
  ```
- **Example Response**:
  ```json
  {
    "id": 2,
    "questionText": "What is JPA?",
    "quizId": 1,
    "answers": [
      {
        "id": 3,
        "answerText": "Java Persistence API",
        "isCorrect": true,
        "questionId": 2
      },
      {
        "id": 4,
        "answerText": "Java Programming Application",
        "isCorrect": false,
        "questionId": 2
      }
    ]
  }
  ```

### Update Quiz Question

Updates an existing quiz question.

- **URL**: `/quiz-questions/{id}`
- **Method**: `PUT`
- **URL Parameters**:
  - `id`: Question ID
- **Request Body**:
  ```json
  {
    "questionText": "Updated question text",
    "quizId": 1,
    "answers": [
      {
        "id": 3,
        "answerText": "Updated answer",
        "isCorrect": true
      },
      {
        "id": 4,
        "answerText": "Another answer",
        "isCorrect": false
      }
    ]
  }
  ```
- **Example Response**: Same format as Create Quiz Question

### Delete Quiz Question

Deletes a quiz question.

- **URL**: `/quiz-questions/{id}`
- **Method**: `DELETE`
- **URL Parameters**:
  - `id`: Question ID
- **Example Request**:
  ```
  DELETE /api/quiz-questions/2
  ```
- **Example Response**: 204 No Content

## Quiz Answers

### Get All Quiz Answers

Returns a list of all quiz answers.

- **URL**: `/quiz-answers`
- **Method**: `GET`
- **Example Request**:
  ```
  GET /api/quiz-answers
  ```
- **Example Response**:
  ```json
  [
    {
      "id": 1,
      "answerText": "A Java framework",
      "isCorrect": true,
      "questionId": 1
    },
    {
      "id": 2,
      "answerText": "A JavaScript library",
      "isCorrect": false,
      "questionId": 1
    }
  ]
  ```

### Get Quiz Answer by ID

Returns details of a specific quiz answer.

- **URL**: `/quiz-answers/{id}`
- **Method**: `GET`
- **URL Parameters**:
  - `id`: Answer ID
- **Example Request**:
  ```
  GET /api/quiz-answers/1
  ```
- **Example Response**:
  ```json
  {
    "id": 1,
    "answerText": "A Java framework",
    "isCorrect": true,
    "questionId": 1
  }
  ```

### Get Quiz Answers by Question ID

Returns all answers for a specific question.

- **URL**: `/quiz-answers/question/{questionId}`
- **Method**: `GET`
- **URL Parameters**:
  - `questionId`: Question ID
- **Example Request**:
  ```
  GET /api/quiz-answers/question/1
  ```
- **Example Response**: Array of answer objects as in Get All Quiz Answers

### Get Correct Quiz Answers by Question ID

Returns all correct answers for a specific question.

- **URL**: `/quiz-answers/question/{questionId}/correct`
- **Method**: `GET`
- **URL Parameters**:
  - `questionId`: Question ID
- **Example Request**:
  ```
  GET /api/quiz-answers/question/1/correct
  ```
- **Example Response**: Array of answer objects as in Get All Quiz Answers (only correct ones)

### Create Quiz Answer

Creates a new quiz answer.

- **URL**: `/quiz-answers`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "answerText": "A design pattern",
    "isCorrect": false,
    "questionId": 1
  }
  ```
- **Example Response**:
  ```json
  {
    "id": 3,
    "answerText": "A design pattern",
    "isCorrect": false,
    "questionId": 1
  }
  ```

### Update Quiz Answer

Updates an existing quiz answer.

- **URL**: `/quiz-answers/{id}`
- **Method**: `PUT`
- **URL Parameters**:
  - `id`: Answer ID
- **Request Body**:
  ```json
  {
    "answerText": "Updated answer text",
    "isCorrect": true,
    "questionId": 1
  }
  ```
- **Example Response**: Same format as Create Quiz Answer

### Delete Quiz Answer

Deletes a quiz answer.

- **URL**: `/quiz-answers/{id}`
- **Method**: `DELETE`
- **URL Parameters**:
  - `id`: Answer ID
- **Example Request**:
  ```
  DELETE /api/quiz-answers/3
  ```
- **Example Response**: 204 No Content

## Quiz Results

### Get All Quiz Results

Returns a list of all quiz results.

- **URL**: `/quiz-results`
- **Method**: `GET`
- **Example Request**:
  ```
  GET /api/quiz-results
  ```
- **Example Response**:
  ```json
  [
    {
      "id": 1,
      "userId": "user123",
      "quizId": 1,
      "quizTitle": "Spring Boot Basics Quiz",
      "moduleId": 1,
      "moduleTitle": "Getting Started",
      "courseId": 1,
      "courseTitle": "Introduction to Spring Boot",
      "score": 80,
      "submittedAt": "2025-08-27T15:30:45.123456"
    }
  ]
  ```

### Get Quiz Result by ID

Returns details of a specific quiz result.

- **URL**: `/quiz-results/{id}`
- **Method**: `GET`
- **URL Parameters**:
  - `id`: Result ID
- **Example Request**:
  ```
  GET /api/quiz-results/1
  ```
- **Example Response**:
  ```json
  {
    "id": 1,
    "userId": "user123",
    "quizId": 1,
    "quizTitle": "Spring Boot Basics Quiz",
    "moduleId": 1,
    "moduleTitle": "Getting Started",
    "courseId": 1,
    "courseTitle": "Introduction to Spring Boot",
    "score": 80,
    "submittedAt": "2025-08-27T15:30:45.123456"
  }
  ```

### Get Quiz Results by User ID

Returns all quiz results for a specific user.

- **URL**: `/quiz-results/user/{userId}`
- **Method**: `GET`
- **URL Parameters**:
  - `userId`: User ID
- **Example Request**:
  ```
  GET /api/quiz-results/user/user123
  ```
- **Example Response**: Array of result objects as in Get All Quiz Results

### Get Quiz Results by Quiz ID

Returns all results for a specific quiz.

- **URL**: `/quiz-results/quiz/{quizId}`
- **Method**: `GET`
- **URL Parameters**:
  - `quizId`: Quiz ID
- **Example Request**:
  ```
  GET /api/quiz-results/quiz/1
  ```
- **Example Response**: Array of result objects as in Get All Quiz Results

### Get Quiz Results by User ID and Quiz ID

Returns all results for a specific user on a specific quiz.

- **URL**: `/quiz-results/user/{userId}/quiz/{quizId}`
- **Method**: `GET`
- **URL Parameters**:
  - `userId`: User ID
  - `quizId`: Quiz ID
- **Example Request**:
  ```
  GET /api/quiz-results/user/user123/quiz/1
  ```
- **Example Response**: Array of result objects as in Get All Quiz Results

### Get Best Quiz Result for User

Returns the best result for a specific user on a specific quiz.

- **URL**: `/quiz-results/user/{userId}/quiz/{quizId}/best`
- **Method**: `GET`
- **URL Parameters**:
  - `userId`: User ID
  - `quizId`: Quiz ID
- **Example Request**:
  ```
  GET /api/quiz-results/user/user123/quiz/1/best
  ```
- **Example Response**: Result object as in Get Quiz Result by ID

### Create Quiz Result

Creates a new quiz result.

- **URL**: `/quiz-results`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "userId": "user123",
    "quizId": 1,
    "score": 90
  }
  ```
- **Example Response**:
  ```json
  {
    "id": 2,
    "userId": "user123",
    "quizId": 1,
    "quizTitle": "Spring Boot Basics Quiz",
    "moduleId": 1,
    "moduleTitle": "Getting Started",
    "courseId": 1,
    "courseTitle": "Introduction to Spring Boot",
    "score": 90,
    "submittedAt": "2025-08-28T12:34:56.789012"
  }
  ```

### Delete Quiz Result

Deletes a quiz result.

- **URL**: `/quiz-results/{id}`
- **Method**: `DELETE`
- **URL Parameters**:
  - `id`: Result ID
- **Example Request**:
  ```
  DELETE /api/quiz-results/2
  ```
- **Example Response**: 204 No Content

## Error Handling

All endpoints return appropriate HTTP status codes:

- `200 OK`: Successful request
- `201 Created`: Resource successfully created
- `204 No Content`: Resource successfully deleted
- `400 Bad Request`: Invalid request parameters
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

Error responses include details about the error:

```json
{
  "timestamp": "2025-08-28T12:34:56.789012",
  "status": 404,
  "error": "Not Found",
  "message": "Course with ID 100 not found",
  "path": "/api/courses/100"
}
```

## Data Models

### Course
```json
{
  "id": 1,
  "title": "Introduction to Spring Boot",
  "description": "Learn the basics of Spring Boot",
  "instructorId": "instructor123",
  "instructorName": "John Doe",
  "category": "Programming",
  "createdAt": "2025-08-27T00:12:12.292221",
  "moduleCount": 2,
  "enrollmentCount": 10,
  "completionPercentage": 0.0,
  "modules": [...],
  "userEnrolled": false
}
```

### Module
```json
{
  "id": 1,
  "title": "Getting Started",
  "type": "VIDEO",
  "contentUrl": "https://storage.example.com/videos/intro.mp4",
  "moduleOrder": 1,
  "completed": false,
  "courseId": 1,
  "courseTitle": "Introduction to Spring Boot",
  "quiz": {...}
}
```

### Enrollment
```json
{
  "id": 1,
  "userId": "user123",
  "courseId": 1,
  "courseTitle": "Introduction to Spring Boot",
  "enrolledAt": "2025-08-25T10:15:30.123456"
}
```

### Progress
```json
{
  "id": 1,
  "userId": "user123",
  "moduleId": 1,
  "moduleTitle": "Getting Started",
  "courseId": 1,
  "courseTitle": "Introduction to Spring Boot",
  "completed": true,
  "completedAt": "2025-08-27T15:30:45.123456"
}
```

### Quiz
```json
{
  "id": 1,
  "title": "Spring Boot Basics Quiz",
  "moduleId": 1,
  "questions": [...]
}
```

### Quiz Question
```json
{
  "id": 1,
  "questionText": "What is Spring Boot?",
  "quizId": 1,
  "answers": [...]
}
```

### Quiz Answer
```json
{
  "id": 1,
  "answerText": "A Java framework",
  "isCorrect": true,
  "questionId": 1
}
```

### Quiz Result
```json
{
  "id": 1,
  "userId": "user123",
  "quizId": 1,
  "quizTitle": "Spring Boot Basics Quiz",
  "moduleId": 1,
  "moduleTitle": "Getting Started",
  "courseId": 1,
  "courseTitle": "Introduction to Spring Boot",
  "score": 80,
  "submittedAt": "2025-08-27T15:30:45.123456"
}
```
