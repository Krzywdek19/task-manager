# Task Manager API

## About

Task Manager API is a backend application for managing small collaborative projects and their tasks.

The application allows users to create projects, organize work into tasks, assign users, track task status and priority, and filter and paginate project tasks.

The project was built as a practical backend application focused on REST API design, authentication and authorization, relational data modeling, database migrations, validation, error handling, and automated testing.

## Features

- User registration and login with JWT authentication
- Project creation and management
- Project ownership and resource-level authorization
- Task CRUD operations
- Task status management
- Task priorities
- Assigning and unassigning users from tasks
- Filtering tasks by status and priority
- Pagination and sorting
- Request validation
- Global exception handling
- PostgreSQL persistence
- Flyway database migrations
- Unit and integration tests

## Tech Stack

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Hibernate
- Spring Security
- JWT
- Bean Validation
- PostgreSQL
- Flyway
- Maven
- Docker Compose
- JUnit 5
- Mockito
- AssertJ
- Testcontainers

## Architecture

The application uses a simple layered architecture with clear separation of responsibilities.

- Controller layer - exposes REST endpoints, handles HTTP requests, request validation and authentication context.
- Service layer - contains business logic, authorization checks and transactional operations.
- Repository layer - provides database access using Spring Data JPA.
- Entity layer - represents the persistence model and relationships between users, projects and tasks.
- DTO layer - separates the public API contract from JPA entities.
- Mapper layer - converts entities into API response DTOs.
- Security layer - handles authentication with JWT and integrates with Spring Security.
- Exception layer - provides domain-specific exceptions and centralized error handling through @RestControllerAdvice.

The project intentionally uses a straightforward layered architecture instead of introducing more complex patterns such as Hexagonal or Clean Architecture.

The goal is to keep the application easy to understand while maintaining separation of concerns, low coupling between layers and good testability.

Business operations are handled inside transactional service methods.

JPA entities are not exposed directly through the REST API. Requests and responses use dedicated DTOs instead.

The general request flow looks like:

Client
|
v
Controller
|
v
Service
|
v
Repository
|
v
PostgreSQL

Additional components such as mappers, security and exception handling support this flow without mixing their responsibilities with business logic.

## Domain Model

### User

Represents an application user.

Main fields:

- id
- email
- passwordHash
- roles

Users can authenticate to the application and can be assigned to tasks.

A user can also own multiple projects.

### Project

Represents a project created by a user.

Main fields:

- id
- name
- description
- owner
- createdAt

Each project belongs to exactly one owner.

Relationship:

User 1 ----- N Project

A single user can own multiple projects, while every project has exactly one owner.

### Task

Represents a unit of work inside a project.

Main fields:

- id
- title
- description
- status
- priority
- project
- assignees
- createdAt
- updatedAt

Each task belongs to exactly one project.

Relationship:

Project 1 ----- N Task

A task can also be assigned to multiple users, while one user can participate in multiple tasks.

Relationship:

Task N ----- M User

The many-to-many relationship is represented in the database by the task_assignees join table.

### Task Status

Supported task states:

TODO
IN_PROGRESS
DONE

### Task Priority

Supported priority levels:

LOW
MEDIUM
HIGH

### Domain Overview

User
|
| owns
v
Project
|
| contains
v
Task <------> User
assigned to

Project ownership is used as part of resource-level authorization.

Project and task operations verify that the authenticated user owns the project before allowing access or modification.

This prevents users from accessing or modifying resources belonging to another project owner.

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/auth/register | Register a new user |
| POST | /api/auth/login | Authenticate user and receive JWT |

### Projects

All project endpoints require authentication.

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/projects | Create a project |
| GET | /api/projects | Get projects owned by the authenticated user |
| GET | /api/projects/{projectId} | Get a project by ID |
| PUT | /api/projects/{projectId} | Update a project |
| DELETE | /api/projects/{projectId} | Delete a project |

Project access is restricted to the project owner.

### Tasks

Task endpoints are nested under projects.

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/projects/{projectId}/tasks | Create a task |
| GET | /api/projects/{projectId}/tasks | Get project tasks |
| GET | /api/projects/{projectId}/tasks/{taskId} | Get a single task |
| PUT | /api/projects/{projectId}/tasks/{taskId} | Update task data |
| PATCH | /api/projects/{projectId}/tasks/{taskId}/status | Change task status |
| DELETE | /api/projects/{projectId}/tasks/{taskId} | Delete a task |
| PUT | /api/projects/{projectId}/tasks/{taskId}/assignees/{userId} | Assign a user to a task |
| DELETE | /api/projects/{projectId}/tasks/{taskId}/assignees/{userId} | Unassign a user from a task |

### Task Filtering and Pagination

The task list endpoint supports optional filtering and pagination.

Example:

GET /api/projects/{projectId}/tasks?status=TODO&priority=HIGH&page=0&size=20&sort=createdAt,desc

Supported filters:

- status
- priority

Supported pagination parameters:

- page
- size
- sort

Example statuses:

TODO
IN_PROGRESS
DONE

Example priorities:

LOW
MEDIUM
HIGH


## Authentication

The application uses stateless JWT authentication implemented with Spring Security.

Registration and login endpoints are publicly available:

POST /api/auth/register
POST /api/auth/login

Other endpoints require an authenticated request.

After successful login, the API returns a JWT access token.

The token should be included in subsequent requests using the Authorization header:

Authorization: Bearer <token>

The JWT contains the authenticated user's identity and authorities.

The user's email is used as the JWT subject and is later available through the Spring Security Authentication object.

Example:

authentication.getName()

This identity is used in the service layer to perform resource-level authorization.

For example, project and task operations verify that the authenticated user owns the requested project.

Passwords are never stored in plain text. They are hashed using BCrypt before being persisted.

The application uses stateless authentication, so server-side HTTP sessions are not required.


## Example Requests

### Register

POST /api/auth/register

Content-Type: application/json

{
"email": "user@example.com",
"password": "StrongPassword1!"
}

### Login

POST /api/auth/login

Content-Type: application/json

{
"email": "user@example.com",
"password": "StrongPassword1!"
}

### Create Project

POST /api/projects

Authorization: Bearer <token>
Content-Type: application/json

{
"name": "University Project",
"description": "Project used to prepare a final report"
}

### Create Task

POST /api/projects/{projectId}/tasks

Authorization: Bearer <token>
Content-Type: application/json

{
"title": "Prepare calculations",
"description": "Calculate results required for the report",
"priority": "HIGH"
}

New tasks are created with the TODO status.

### Change Task Status

PATCH /api/projects/{projectId}/tasks/{taskId}/status

Authorization: Bearer <token>
Content-Type: application/json

{
"status": "IN_PROGRESS"
}

### Assign User to Task

PUT /api/projects/{projectId}/tasks/{taskId}/assignees/{userId}

Authorization: Bearer <token>


## Running Locally

### Requirements

Make sure the following tools are installed:

- Java 21+
- Maven
- Docker
- Docker Compose

### 1. Clone the repository

git clone <repository-url>

cd task-manager

### 2. Configure environment variables

Create a .env file in the project root.

Example:

DB_URL=jdbc:postgresql://localhost:5432/taskmanager
DB_USERNAME=taskmanager
DB_PASSWORD=taskmanager
JWT_SECRET=replace-with-a-secure-secret-key

A sample environment file is available as:

.env.example

The real .env file should not be committed to Git.

### 3. Start PostgreSQL

docker compose up -d

### 4. Start the application

mvn spring-boot:run

During application startup, Flyway automatically applies database migrations.

The application uses Hibernate schema validation instead of automatic schema generation.

Relevant configuration:

spring.jpa.hibernate.ddl-auto=validate

This ensures that the database schema is managed through Flyway migrations rather than Hibernate.


## Docker

Docker Compose is used to run PostgreSQL locally.

The database runs in a container while the Spring Boot application can be started directly from the development environment or Maven.

Example:

docker compose up -d

To stop the containers:

docker compose down

PostgreSQL data is persisted using a Docker volume.

Using Docker provides a reproducible local database environment without requiring PostgreSQL to be installed directly on the host system.


## Database Migrations

Database schema changes are managed using Flyway.

Migration files are stored in:

src/main/resources/db/migration

The migrations create and evolve tables used by the application, including:

- users
- user_roles
- projects
- tasks
- task_assignees

Flyway migrations are executed automatically during application startup.

Hibernate is configured with:

ddl-auto: validate

This means Hibernate verifies that entity mappings match the database schema but does not modify the schema automatically.

This separation makes database changes explicit, versioned and reproducible.


## Testing

The project contains several levels of automated tests.

### Unit Tests

Service-layer logic is tested using JUnit 5, Mockito and AssertJ.

Repository, mapper and other dependencies are mocked so that business logic can be tested independently.

Examples of tested scenarios include:

- task creation
- task updates
- task status changes
- assigning users to tasks
- unassigning users
- handling missing users and tasks
- pagination and filtering logic

### Repository Integration Tests

Repository tests use a real PostgreSQL instance started with Testcontainers.

These tests verify actual JPA mappings and database constraints instead of relying on an in-memory database.

### Service Integration Tests

Integration tests verify cooperation between:

- service layer
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway migrations

One of the tested flows verifies that assigning and unassigning a user from a task is correctly persisted in the task_assignees join table.

The persistence context is cleared before verification to make sure data is read again from the database instead of only being checked in memory.

### API Integration Tests

MockMvc is used together with the Spring application context to test HTTP endpoints.

These tests can verify:

- HTTP status codes
- JSON request and response handling
- Spring Security
- authentication
- validation
- controller-service-repository flow
- persistence in PostgreSQL

Spring Security Test is used to provide authenticated JWT requests where the JWT mechanism itself is not the subject of the test.

### Running Tests

Run all tests with:

mvn test


## Design Decisions

### DTOs Instead of Exposing Entities

JPA entities are not returned directly from controllers.

Dedicated request and response DTOs are used to define the public API contract.

This prevents persistence implementation details from leaking into the API and gives more control over validation and response structure.


### Service Layer

Controllers are kept focused on HTTP concerns.

Business logic, authorization checks and transactional operations are placed in service classes.

This improves separation of concerns and makes the business logic easier to test.


### Constructor Injection

Dependencies are provided through constructor injection.

Lombok @RequiredArgsConstructor is used to reduce constructor boilerplate.

Constructor injection makes dependencies explicit and improves testability.


### Project Ownership Authorization

The authenticated user's identity comes from the JWT rather than from request data.

The client cannot select the project owner by sending an arbitrary email or user ID.

Repository queries include ownership information when accessing protected resources.

For resources that do not belong to the authenticated user, the API returns a not-found response instead of exposing information about another user's resources.


### LAZY Relationships

JPA relationships are generally configured using LAZY fetching.

This avoids loading related objects automatically when they are not required by the current use case.

Fetching strategies are selected depending on how the data is used.


### EntityGraph

EntityGraph is used for queries where related data is required immediately.

For example, task details may require assignee information during mapping.

Using a query-specific fetch plan avoids changing the relationship globally to EAGER.


### Pagination and Collection Fetching

A collection relationship such as task assignees is not blindly fetched using EntityGraph together with Pageable.

A to-many join can produce multiple SQL rows for a single task.

This conflicts with pagination because the database paginates SQL rows while the application expects pages of unique Task entities.

For paginated task queries, tasks are paginated first and collection fetching is handled separately.


### Batch Fetching

Lazy-loaded task assignees can be batch-fetched to reduce the N+1 query problem.

Instead of executing one query for every task's assignees, Hibernate can load collections for several tasks in a single query.

This keeps pagination correct while reducing unnecessary database round trips.


### Set for Task Assignees

Task assignees are represented using Set<User>.

Multiple identical assignments do not make sense in the domain, so a Set represents the relationship more naturally than a List.

Assignment logic also explicitly checks user IDs instead of relying only on entity reference equality.


### Idempotent User Assignment

Assigning a user uses:

PUT /tasks/{taskId}/assignees/{userId}

The operation is idempotent.

Assigning the same user multiple times results in the same final resource state.


### PATCH for Task Status

Task status changes use a dedicated PATCH endpoint:

PATCH /tasks/{taskId}/status

The endpoint modifies only one part of the Task resource instead of replacing the complete task representation.


### Dirty Checking

Entities loaded inside transactional service methods are managed by Hibernate.

After modifying a managed entity, calling repository.save() again is not required.

Hibernate detects changes using dirty checking and synchronizes them with the database during flush or transaction commit.


### Explicit Flush Before Mapping Updated Entities

Some update operations call flush before producing the response.

This ensures that lifecycle callbacks such as @PreUpdate have already executed before the entity is converted into the response DTO.

This is especially relevant for fields such as updatedAt.


### Resource-Level Validation

The API does not only check whether the user is authenticated.

It also verifies whether the user is allowed to access a specific project or task.

This distinction separates:

Authentication - who is the user?

Authorization - is the user allowed to perform this operation on this resource?


### Global Exception Handling

Domain-specific exceptions are converted into consistent API error responses using @RestControllerAdvice.

Examples include:

- validation errors
- duplicate email
- invalid credentials
- project not found
- task not found
- user not found
- invalid task assignment state

This keeps exception-to-HTTP mapping outside controllers.


### Flyway Instead of Automatic Schema Generation

Flyway is responsible for creating and modifying the database schema.

Hibernate only validates the schema.

This makes database evolution explicit and allows schema changes to be tracked together with application code.


### PostgreSQL Testcontainers

Integration tests use PostgreSQL through Testcontainers instead of replacing production persistence with an in-memory database.

This reduces differences between the test and production database environments.


## Future Improvements

### Task Workspace and File Management

A planned extension is a workspace associated with each task.

A task could contain multiple folders representing different parts of collaborative work.

Example:

Task: Prepare final report

Information
- source-data.csv
- sources.txt

Calculations
- results.xlsx
- methodology.txt

Result
- final-report.pdf

Folders could optionally have a responsible user.

Possible domain model:

Task
|
| 1:N
v
TaskFolder
|
| 1:N
v
TaskAttachment

File metadata would be stored in PostgreSQL while file contents could initially be stored in local Docker-backed storage.

A future version could replace local storage with S3-compatible object storage.


### Project Collaboration

The current authorization model is primarily based on project ownership.

A future version could introduce explicit project membership with roles such as:

- OWNER
- MEMBER

This would allow assigned users to access project resources without transferring ownership.


### Fine-Grained Permissions

Future authorization rules could distinguish between:

- project owner
- project member
- task assignee
- folder owner

This would enable more realistic collaborative workflows.


### File Security

File support would require additional security mechanisms such as:

- file size limits
- file type validation
- generated storage keys
- protected download endpoints
- ownership and membership checks


### Task Search

Task listing could be extended with:

- title search
- description search
- assignee filtering
- creation date filtering
- additional sorting options


### Optimistic Locking

A @Version field could be introduced to protect resources from lost updates when multiple users modify the same task concurrently.


### Refresh Tokens

Authentication could be extended with refresh tokens and access token rotation.


### Audit History

The application could keep a history of important domain changes such as:

- task status changes
- assignee changes
- file uploads
- project membership changes


### Subtasks

Tasks could optionally contain subtasks for workflows where a larger piece of work needs separate statuses, priorities and assignees.


## Project Status

The current version provides a complete backend core for project and task management.

Implemented areas include:

- authentication
- authorization
- projects
- task management
- task assignment
- task status tracking
- filtering
- pagination
- validation
- error handling
- database migrations
- automated testing

The next planned development stage is collaborative task workspaces with folders and file attachments.