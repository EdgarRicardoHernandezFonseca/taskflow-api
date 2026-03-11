# TasksFlow API

TasksFlow is a RESTful API built with **Java and Spring Boot** that allows users to manage tasks in a secure and scalable way.

The project includes **user authentication using JWT**, task management features, and a clean layered architecture following backend development best practices.

This project demonstrates backend engineering skills in:

- API development
- Authentication and security
- Backend architecture
- Database integration

---

# 🚀 Features

- User registration
- Secure authentication using **JWT**
- Refresh Token Rotation
- Token Reuse Detection
- Device-bound Refresh Tokens
- Impossible Travel Detection
- Task management (CRUD operations)
- Tasks associated with authenticated users
- Input validation
- Global exception handling
- Clean layered architecture

---

# 🛠 Tech Stack

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

# 📂 Project Structure

The application follows a **layered architecture** commonly used in professional Spring Boot applications.


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

# 🔐 Authentication Flow

The API uses **JWT authentication**.

1️⃣ User registers an account  
2️⃣ User logs in with credentials  
3️⃣ Server generates a **JWT access token and refresh token**  
4️⃣ Client sends the access token in every request  

Example request header:


Authorization: Bearer <JWT_TOKEN>


---

# 📡 API Endpoints

## Authentication

| Method | Endpoint | Description |
|------|------|------|
| POST | /auth/register | Register a new user |
| POST | /auth/login | Authenticate user |
| POST | /auth/refresh | Refresh access token |

---

## Tasks

| Method | Endpoint | Description |
|------|------|------|
| GET | /tasks | Get all tasks |
| GET | /tasks/{id} | Get task by ID |
| POST | /tasks | Create a new task |
| PUT | /tasks/{id} | Update a task |
| DELETE | /tasks/{id} | Delete a task |

---

# Taskflow Architecture Diagrams

1️⃣ Authentication System Architecture

This diagram shows the main backend components involved in authentication and security.

<p align="center"> <img src="https://github.com/user-attachments/assets/a9ee356c-af58-493a-9f3a-b10fa2966273" width="900"/> </p>
2️⃣ Authentication Flow (Login)

Complete login process from the client request to token generation.

<p align="center"> <img src="https://github.com/user-attachments/assets/5199879c-100a-4d86-9cd1-58b20e5e4aef" width="900"/> </p>
3️⃣ Refresh Token Rotation Flow

Illustrates how the system rotates refresh tokens to improve security.

<p align="center"> <img src="https://github.com/user-attachments/assets/cc781340-01af-4167-86bd-2f479c6effeb" width="900"/> </p>
4️⃣ Token Reuse Attack Detection

Shows how the system detects refresh token reuse attacks and revokes the token family.

<p align="center"> <img src="https://github.com/user-attachments/assets/9cd76c91-4bb8-4142-b937-3a90f364fec4" width="900"/> </p>

---

# 👨‍💻 Author

Edgar Ricardo Hernández Fonseca

Backend Developer

Java | Spring Boot | REST APIs | AWS

---

# 🎯 Purpose of the Project

This project was created to demonstrate backend development capabilities including:

Secure REST API design

Authentication and authorization

Scalable backend architecture

Database integration using Spring Data JPA

It can serve as a starting point for real-world task management systems or SaaS applications.

---

# 💼 Freelance Services

I can help you build or customize:

REST APIs with Java and Spring Boot

Authentication systems (JWT / OAuth2)

Backend architecture for scalable applications

Database design and integration

Microservices and cloud-ready backend systems

Feel free to contact me if you need a backend developer for your project.

---

# ⚙️ Running the Project

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

```mermaid

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
🏗 Authentication Architecture

This project implements a production-style authentication system similar to systems used by companies like
Auth0 and Okta.

The authentication system includes:

JWT Access Tokens

Refresh Token Rotation

Token Reuse Detection

Device-bound Refresh Tokens

Impossible Travel Detection

Session Management

Security Event Logging

---