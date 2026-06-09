
# Steps:

## 1. run psql shell command :

CREATE DATABASE empmanelite;

## 2. set environment variables :

LOCAL_DB_USERNAME = your_postgres_user
LOCAL_DB_PASSWORD = your_postgres_password
JWT_SECRET = your_hs512_hex_jwt_secret

## 3. run app :

/src/main/java/com/elite/employeemanager/EmployeemanagerApplication.java

---

# Employee Manager API Documentation (v1)

## Base URL
*   `http://localhost:8080/api/v1` (or your configured environment base URL)

## Authorization & Headers
*   **Content-Type:** `application/json` (except for the DELETE endpoint, which takes `text/plain`)
*   **Protected Endpoints:** All `/employees` endpoints require a Bearer token in the header:
    `Authorization: Bearer <access_token>`
*   **Required Authority:** `EMPLOYEE_MANAGE` (automatically checked by the backend)

---

## 1. Authentication Endpoints
**Base Path:** `/api/v1/auth` (Publicly accessible)

### 1.1. Login
*   **HTTP Method:** `POST`
*   **Path:** `/login`
*   **Description:** Authenticates a user and returns JWT access & refresh tokens along with user information, roles, permissions, and viewable components.
*   **Request Payload ([LoginRequest.java](file:///c:/Users/Akilesh/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/auth/jwt/dto/LoginRequest.java)):**
    ```json
    {
      "email": "admin@company.com",
      "password": "securepassword123"
    }
    ```
*   **Success Response (200 OK - [AuthenticationResponse.java](file:///c:/Users/Akilesh/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/auth/jwt/dto/AuthenticationResponse.java)):**
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiIsIn...", // JWT access token
      "refresh": "550e8400-e29b-41d4-a716-446655440000", // UUID refresh token
      "user": {
        "id": 1,
        "email": "admin@company.com",
        "roles": ["ADMIN"],
        "permissions": ["EMPLOYEE_MANAGE"],
        "components": ["DASHBOARD", "EMPLOYEE_LIST"]
      }
    }
    ```

### 1.2. Refresh Access Token
*   **HTTP Method:** `POST`
*   **Path:** `/refresh`
*   **Description:** Requests a new short-lived access token using a valid refresh token.
*   **Request Payload ([RefreshTokenRequest.java](file:///c:/Users/Akilesh/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/auth/jwt/dto/RefreshTokenRequest.java)):**
    ```json
    {
      "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
    }
    ```
*   **Success Response (200 OK):**
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiIsInNewToken...",
      "refresh": "550e8400-e29b-41d4-a716-446655440000",
      "user": {
        "id": 1,
        "email": "admin@company.com",
        "roles": ["ADMIN"],
        "permissions": ["EMPLOYEE_MANAGE"],
        "components": ["DASHBOARD", "EMPLOYEE_LIST"]
      }
    }
    ```

---

## 2. Employee Management Endpoints
**Base Path:** `/api/v1/employees` (Requires `Authorization: Bearer <access_token_of_ROLE_ADMIN>`)

### 2.1. Get All Employees
*   **HTTP Method:** `GET`
*   **Path:** `/`
*   **Description:** Retrieves a list of all active (non-deleted) employees.
*   **Request Payload:** None
*   **Success Response (200 OK - List of [Employee.java](file:///c:/Users/Akilesh/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/employee/entity/Employee.java)):**
    ```json
    [
      {
        "id": 1,
        "employeeCode": "EMP001",
        "name": "Jane Doe",
        "workEmail": "jane.doe@company.com",
        "personalEmail": "jane.doe.personal@gmail.com",
        "phone": "+1234567890",
        "designation": "Software Engineer",
        "joiningDate": "2026-06-01",
        "status": "ACTIVE",
        "notificationPreference": "ALL",
        "profileImage": "https://example.com/avatar.jpg",
        "roles": ["EMPLOYEE"],
        "user": {
          "id": 2,
          "email": "jane.doe@company.com",
          "passwordHash": "$2a$10$...",
          "passwordLastUpdatedAt": "2026-06-09T10:00:00",
          "isActive": true,
          "lastLogin": null,
          "failedLoginAttempts": 0,
          "accountLockedUntil": null,
          "forcePasswordChange": false,
          "createdAt": "2026-06-09T10:00:00",
          "updatedAt": "2026-06-09T10:00:00"
        },
        "createdAt": "2026-06-09T10:00:00",
        "createdBy": 1,
        "updatedAt": "2026-06-09T10:00:00",
        "updatedBy": null,
        "deletedAt": null,
        "deletedBy": null,
        "deleteReason": null
      }
    ]
    ```

### 2.2. Get Employee By ID
*   **HTTP Method:** `GET`
*   **Path:** `/{id}`
*   **Description:** Retrieves a single active employee's details by their ID.
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the employee.
*   **Request Payload:** None
*   **Success Response (200 OK):**
    *   Returns a single [Employee](file:///c:/Users/Akilesh/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/employee/entity/Employee.java) JSON object (same structure as above).
*   **Error Response (404 Not Found):** Returned if the employee ID does not exist or has been deleted.

### 2.3. Add Employee
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Description:** Creates a new employee record and configures their initial login user credentials.
*   **Request Payload ([Employee.java](file:///c:/Users/Akilesh/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/employee/entity/Employee.java)):**
    ```json
    {
      "employeeCode": "EMP002",
      "name": "John Smith",
      "workEmail": "john.smith@company.com",          // Must be unique
      "personalEmail": "john.smith.personal@gmail.com",
      "phone": "+1987654321",
      "designation": "Designer",
      "joiningDate": "2026-06-05",                    // Format: YYYY-MM-DD
      "status": "ACTIVE",                             // Optional. Allowed: "ACTIVE", "INACTIVE", "ON_LEAVE" (Defaults to "ACTIVE")
      "notificationPreference": "ALL",                // Optional. Allowed: "EMAIL", "WHATSAPP", "TEAMS", "ALL" (Defaults to "ALL")
      "profileImage": "https://example.com/john.jpg", // Optional
      "roles": ["Employee"],                          // Optional. List of roles (Defaults to ["Employee"] if omitted)
      "user": {
        "password": "temporaryPassword123"            // Required. Used to create the initial user login password
      }
    }
    ```
*   **Success Response (200 OK):**
    *   Returns the newly created [Employee](file:///c:/Users/Akilesh/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/employee/entity/Employee.java) object including auto-generated IDs, audit timestamps, and hashed password details.

### 2.4. Update Employee
*   **HTTP Method:** `PUT`
*   **Path:** `/{id}`
*   **Description:** Updates an existing employee. Partial updates are supported (only fields present in the payload will be updated).
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the employee to update.
*   **Request Payload (All fields optional):**
    ```json
    {
      "employeeCode": "EMP002-Updated",
      "name": "John Smith Jr.",
      "workEmail": "john.smith.new@company.com", // If changed, also updates user login email
      "personalEmail": "john.smith.newpersonal@gmail.com",
      "phone": "+1987654321",
      "designation": "Lead Designer",
      "joiningDate": "2026-06-05",
      "status": "ON_LEAVE",                      // If set to "INACTIVE", disables the linked user login account
      "notificationPreference": "EMAIL",
      "profileImage": "https://example.com/john-new.jpg",
      "roles": ["Employee", "Admin"],            // Replaces all existing assigned roles
      "user": {
        "password": "newSecurePassword123"       // Optional. Changes the user's password if provided
      }
    }
    ```
*   **Success Response (200 OK):**
    *   Returns the updated [Employee](file:///c:/Users/Akilesh/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/employee/entity/Employee.java) object.

### 2.5. Delete Employee (Soft Delete)
*   **HTTP Method:** `DELETE`
*   **Path:** `/{id}`
*   **Description:** Performs a soft delete on the employee. Sets the status to `"INACTIVE"`, disables the associated user login, and records the deletion metadata.
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the employee.
*   **Request Payload (`text/plain`):**
    *   Pass the reason for deletion as a raw text string.
    *   *Example Raw Body:* `"Resigned to pursue higher education."`
*   **Success Response (204 No Content):**
    *   *Headers:* `Status: 204 No Content`
    *   *Body:* `"Employee Deleted"` (Please note: Some client HTTP packages require ignoring response bodies on a `240/204` status, but the backend sends this text).

---

## 3. General Error Response Format
When any client request fails (e.g., validations fail, unique constraints are violated, or resources are not found), the API returns standard HTTP status codes along with a JSON body:
```json
{
  "timestamp": "2026-06-09T10:55:00.123+00:00",
  "status": 400, // e.g., 400 (Bad Request), 401 (Unauthorized), 404 (Not Found), 409 (Conflict)
  "error": "Conflict",
  "message": "Email already exists", // Detailed validation or business error message
  "path": "/api/v1/employees"
}
```


