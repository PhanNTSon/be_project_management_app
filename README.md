# Backend - Project Management Application

This is the backend backend API for the Project Management Application. It provides complete RESTful endpoints for authentication, project management, user roles, and system resources.

## 🚀 Technologies Used
- **Framework:** Spring Boot 3.x
- **Language:** Java 21
- **Database:** Microsoft SQL Server
- **Authentication:** JWT (JSON Web Tokens)
- **Database Migrations:** Flyway
- **Mapping:** MapStruct

## 🛠️ Prerequisites
- Java Development Kit (JDK) 21
- Maven (or use the provided `mvnw` wrapper)
- MS SQL Server (if running locally without Docker)

## 💻 Running Locally (Development Mode)

1. **Database Setup:**
   Ensure you have a local MS SQL Server instance running with a database named `SRS_SMART_MANAGEMENT_DB`.

2. **Environment Variables / Properties:**
   Create a `.env` file in this directory with the following variables used by both Docker and local execution:
   ```env
   DB_USERNAME=sa
   DB_PASSWORD=sa_Password123!
   SECRET_KEY_BASE64=Your_Secret_Key_Base64_Encoded
   GEMINI_API_KEY=your_gemini_api_key
   ```

3. **Run the Application:**
   Using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   The backend API will start on `http://localhost:8080`.

## 🐳 Running via Docker (Recommended)

To run the Backend and its required MS SQL Server Database seamlessly without installing local dependencies, use the included Docker Compose setup:

1. **Configure Environment:** Ensure your `.env` file is present in this directory with `DB_USERNAME`, `DB_PASSWORD`, `SECRET_KEY_BASE64`, and `GEMINI_API_KEY`.
2. **Navigate to the root directory of this backend repository.**
3. **Run Docker Compose:**
   ```bash
   docker-compose up -d --build
   ```

**Expected Output & Behavior:**
- Docker will spin up the `backend` container, an `mssql` database container, and a temporary `db-init` container.
- The `SRS_SMART_MANAGEMENT_DB` database is automatically created upon startup.
- The Backend API will be successfully built and served at `http://localhost:8080`.
- The Database is accessible on standard port `1433` using your configured credentials.

**Useful Commands:**
- To view logs of the backend explicitly: `docker logs -f project-management-app-backend-1` (or whatever your folder prefix is, e.g., `docker-compose logs -f backend`).
- To stop the containers: `docker-compose down`

## 📦 Building the JAR

To compile and package the application into a standalone JAR file, run:
```bash
./mvnw clean package
```
The resulting `.jar` file will be located in the `target/` directory.