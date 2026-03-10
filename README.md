# TasksFlow API

TasksFlow is a RESTful API built with **Java and Spring Boot** that allows users to manage tasks in a secure and scalable way.

The project includes **user authentication using JWT**, task management features, and a clean layered architecture following backend development best practices.

This project demonstrates backend engineering skills in **API development, authentication, security, and database integration**.

---

## 🚀 Features

- User registration
- Secure authentication using **JWT**
- Task management (CRUD operations)
- Tasks associated with authenticated users
- Input validation
- Global exception handling
- Clean layered architecture

---

## 🛠 Tech Stack

- **Java 17**
- **Spring Boot**
- **Spring Security**
- **JWT (JSON Web Token)**
- **Spring Data JPA**
- **Hibernate**
- **PostgreSQL / MySQL**
- **Maven**
- **Lombok**

Optional:

- Docker
- Swagger / OpenAPI
- JUnit / Mockito

---

## 📂 Project Structure

The application follows a typical **layered architecture** used in professional Spring Boot applications.

src/main/java/com/tasksflow

controller → REST API endpoints
service → Business logic
repository → Data access layer
model → Database entities
dto → Data transfer objects
security → JWT authentication and security configuration
config → Application configuration
exception → Global error handling

---

## 🔐 Authentication Flow

The API uses **JWT authentication**.

1. User registers an account
2. User logs in with credentials
3. Server generates a **JWT token**
4. Client sends the token in every request

Example header:
Authorization: Bearer <JWT_TOKEN>

---

## 📡 API Endpoints

### Authentication

| Method | Endpoint | Description |
|------|------|------|
| POST | /auth/register | Register a new user |
| POST | /auth/login | Authenticate user |

---

### Tasks

| Method | Endpoint | Description |
|------|------|------|
| GET | /tasks | Get all tasks |
| GET | /tasks/{id} | Get task by ID |
| POST | /tasks | Create a new task |
| PUT | /tasks/{id} | Update a task |
| DELETE | /tasks/{id} | Delete a task |

---

## ⚙️ Running the Project

Clone the repository:

```bash
git clone https://github.com/yourusername/tasksflow.git
cd tasksflow

Build the project:
mvn clean install

Run the application:
mvn spring-boot:run

The API will start at:
http://localhost:8080

🧪 Running Tests
mvn test

🐳 Docker (Optional)

Build the image:
docker build -t tasksflow .

Run the container:
docker run -p 8080:8080 tasksflow

📘 API Documentation

Swagger/OpenAPI can be integrated for interactive documentation.

Example endpoint:
http://localhost:8080/swagger-ui.html



## Authentication Architecture

This project implements a production-style authentication system including:

- JWT Access Tokens
- Refresh Token Rotation
- Token Reuse Detection
- Device-bound Refresh Tokens
- Impossible Travel Detection
- Session Management
- Security Event Logging

### Architecture Diagram

1️⃣  Authentication system architecture diagram

It shows the main components of the backend.



2️⃣  Authentication flow (Login)

Complete login process.



3️⃣ Refresh Token Rotation Flow



4️⃣ Attack flow detected (Token Reuse)



👨‍💻 Author

Edgar Ricardo Hernández Fonseca

Backend Developer
Java | Spring Boot | REST APIs | AWS

🎯 Purpose of the Project

This project was created to demonstrate backend development capabilities including:

Secure REST API design

Authentication and authorization

Scalable backend architecture

Database integration with Spring Data JPA

It can serve as a starting point for real-world task management systems or SaaS applications.

## 💼 Freelance Services

I can help you build or customize:

- REST APIs with **Java and Spring Boot**
- Authentication systems (**JWT / OAuth2**)
- Backend architecture for **scalable applications**
- Database design and integration
- Microservices and cloud-ready backend systems

Feel free to contact me if you need a backend developer for your project.
