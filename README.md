# Ikonex Academy Student Management System

A Spring Boot + MySQL full-stack Student Management System with a lightweight HTML/CSS frontend and REST APIs for class streams, students, subjects, assessments, results, and PDF reporting.

## Features

- Class Stream Management CRUD
- Student Management CRUD and class stream assignment
- Subject Management CRUD and class stream assignment
- Assessment capture with duplicate prevention per student, subject, and term
- Result processing with totals, averages, grading, and ranking per class stream
- PDF report cards for students
- PDF class reports for class streams
- Modular backend structure by feature
- Validation and centralized API error handling

## Database Schema

### `class_streams`
- `id`
- `stream_code` unique
- `stream_name`
- `form_level`
- `active`

### `students`
- `id`
- `admission_number` unique
- `first_name`
- `last_name`
- `gender`
- `date_of_birth`
- `class_stream_id` foreign key to `class_streams.id`

### `subjects`
- `id`
- `subject_code` unique
- `subject_name` unique

### `class_stream_subjects`
- join table for the many-to-many relationship between class streams and subjects

### `assessments`
- `id`
- `student_id` foreign key to `students.id`
- `subject_id` foreign key to `subjects.id`
- `term`
- `score`
- unique constraint on `student_id + subject_id + term`

## Entity Relationships

- One class stream has many students.
- One class stream has many subjects.
- One subject can belong to many class streams.
- One student has many assessments.
- One subject has many assessments.
- Each assessment belongs to exactly one student and one subject.

## API Endpoints

### Class streams
- `GET /api/class-streams`
- `GET /api/class-streams/{id}`
- `POST /api/class-streams`
- `PUT /api/class-streams/{id}`
- `DELETE /api/class-streams/{id}`
- `POST /api/class-streams/{classStreamId}/subjects/{subjectId}`
- `DELETE /api/class-streams/{classStreamId}/subjects/{subjectId}`

### Students
- `GET /api/students`
- `GET /api/students/{id}`
- `POST /api/students`
- `PUT /api/students/{id}`
- `DELETE /api/students/{id}`

### Subjects
- `GET /api/subjects`
- `GET /api/subjects/{id}`
- `POST /api/subjects`
- `PUT /api/subjects/{id}`
- `DELETE /api/subjects/{id}`

### Assessments
- `POST /api/assessments`
- `GET /api/assessments?studentId={id}&term={term}`

### Results
- `GET /api/results/class-streams/{classStreamId}?term={term}`
- `GET /api/results/students/{studentId}?term={term}`

### PDF reports
- `GET /api/reports/class-streams/{classStreamId}?term={term}`
- `GET /api/reports/students/{studentId}?term={term}`

## Service Logic

- Validation is handled at the DTO level and centralized exception handling returns readable API errors.
- Duplicate assessments are rejected by the unique repository lookup before save.
- Results are computed per class stream and term by aggregating assessment scores.
- Ranking is based on total score in descending order, with dense rank behavior for ties.
- Grades are assigned from the average score using the grading helper.
- PDF reports are generated with PDFBox.

## Frontend Pages

- `index.html` - dashboard
- `students.html` - student CRUD and class assignment
- `class-streams.html` - stream CRUD and subject assignment
- `subjects.html` - subject CRUD and stream assignment
- `results.html` - class results, student results, and PDF downloads

## Deployment Steps

1. Create the MySQL database `student_web_app`.
2. Set environment variables for `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` if needed.
3. Run the application with `./mvnw spring-boot:run` or build a jar with `./mvnw clean package`.
4. Open the app in a browser and use the static pages from the root path.
5. For production, point the datasource to your managed MySQL instance and disable `spring.jpa.hibernate.ddl-auto=update` in favor of a migration tool if you add one later.

## Notes

- The frontend is intentionally framework-free and uses minimal JavaScript `fetch` calls.
- Validation errors are returned as JSON and displayed by the frontend scripts.
