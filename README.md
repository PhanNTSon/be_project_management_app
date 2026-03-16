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
   Ensure you have a `.env` file configured in this directory:
   ```env
   SECRET_KEY_BASE64="Your_Secret_Key_Base64_Encoded"
   SPRING_DATASOURCE_URL=jdbc:sqlserver://localhost:1433;databaseName=SRS_SMART_MANAGEMENT_DB;encrypt=true;trustServerCertificate=true
   SPRING_DATASOURCE_USERNAME=sa
   SPRING_DATASOURCE_PASSWORD=your_password
   GEMINI_API_KEY=your_gemini_api_key
   ```

3. **Run the Application:**
   Using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   The backend API will start on `http://localhost:8080`.

## 🐳 Running via Docker (Recommended)

To run the entire ecosystem seamlessly without installing MS SQL Server or Java locally, use Docker Compose from the **root workspace directory** (one level above this folder):

```bash
docker-compose up -d --build
```

- This command will spin up the Backend API, the Frontend Web App, and an MS SQL Server database.
- The `SRS_SMART_MANAGEMENT_DB` database is automatically created upon startup.
- The Backend API will be available at `http://localhost:8080`.
- To view logs: `docker-compose logs -f backend`
- To stop the containers: `docker-compose down`

## 📦 Building the JAR

To compile and package the application into a standalone JAR file, run:
```bash
./mvnw clean package
```
The resulting `.jar` file will be located in the `target/` directory.