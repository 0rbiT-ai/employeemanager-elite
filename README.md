
# Steps:

## 1. run psql shell command :

CREATE DATABASE empmanelite;

## 2. set environment variables :

LOCAL_DB_USERNAME = your_postgres_user\
LOCAL_DB_PASSWORD = your_postgres_password\
JWT_SECRET = your_hs512_hex_jwt_secret\
MAIL_USERNAME = your_email_username_here\
MAIL_PASSWORD = your_email_password_here\
FRONTEND_URL = http://localhost:5173

## 3. run app :

/src/main/java/com/elite/employeemanager/EmployeemanagerApplication.java

---

# API (v1)

## Base Configuration
*   **Base URL:** `http://localhost:8080/api/v1`
*   **Default Headers:**
    *   `Content-Type: application/json`
    *   Protected endpoints require: `Authorization: Bearer <access_token>`

## Role-Based Access Mapping
Access to protected endpoints is governed by authorities compiled from user roles on login:

| Role Code | Role Name | Granted Authorities / Permissions | Allowed Modules / Endpoints |
| :--- | :--- | :--- | :--- |
| **`ADMIN`** | Admin | `EMPLOYEE_MANAGE`, `TEAM_MANAGE`, `PROJECT_MANAGE`, `USER_CREATE`, `TASK_CREATE`, `TASK_ASSIGN`, `TIMESHEET_APPROVE`, `TIMESHEET_SUBMIT` | **All Endpoints** (Authentication, Employees, Teams, Team Members, Projects) |
| **`TEAM_LEAD`** | Team Lead | `TEAM_MANAGE`, `PROJECT_MANAGE`, `TASK_CREATE`, `TASK_ASSIGN`, `TIMESHEET_APPROVE`, `TIMESHEET_SUBMIT` | Authentication, Teams, Team Members, Projects (No Employee Management) |
| **`SUB_LEAD`** | Sub Lead | `TIMESHEET_SUBMIT` (No default administrative permissions) | Authentication only (No Teams/Employee management unless assigned manually) |
| **`EMPLOYEE`** | Employee | `TIMESHEET_SUBMIT` | Authentication only |

---

## 1. Authentication Module
**Base Path:** `/api/v1/auth` (Publicly accessible — no token required)

### 1.1. User Login
*   **HTTP Method:** `POST`
*   **Path:** `/login`
*   **Access Allowed:** Public (All Roles)
*   **Request Body ([LoginRequest](file:///c:/Users/dantd/OneDrive/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/auth/jwt/dto/LoginRequest.java)):**
    ```json
    {
      "email": "employee@teamops.com",
      "password": "employee123"
    }
    ```
*   **Success Response (200 OK - [AuthenticationResponse](file:///c:/Users/dantd/OneDrive/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/auth/jwt/dto/AuthenticationResponse.java)):**
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiIsIn...", // JWT Access Token
      "refresh": "a189f38f-dc42...", // Refresh Token UUID
      "user": {
        "id": 2,
        "email": "employee@teamops.com",
        "roles": ["EMPLOYEE"],
        "permissions": ["TIMESHEET_SUBMIT"],
        "components": [] // UI components that should dynamically load
      }
    }
    ```

### 1.2. Refresh Access Token
*   **HTTP Method:** `POST`
*   **Path:** `/refresh`
*   **Access Allowed:** Public (All Roles)
*   **Request Body ([RefreshTokenRequest](file:///c:/Users/dantd/OneDrive/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/auth/jwt/dto/RefreshTokenRequest.java)):**
    ```json
    {
      "refreshToken": "a189f38f-dc42..."
    }
    ```
*   **Success Response (200 OK):**
    *   Returns updated access token (same response format as `POST /login`).

### 1.3. Forgot Password
*   **HTTP Method:** `POST`
*   **Path:** `/forgot-password`
*   **Access Allowed:** Public (All Roles)
*   **Request Body ([ForgotPasswordRequest](file:///c:/Users/dantd/OneDrive/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/auth/passwordreset/dto/ForgotPasswordRequest.java)):**
    ```json
    {
      "email": "employee@teamops.com"
    }
    ```
*   **Success Response (200 OK):**
    *   *Body:* `"If the email exists, a password reset link has been sent."`

### 1.4. Reset Password
*   **HTTP Method:** `POST`
*   **Path:** `/reset-password`
*   **Access Allowed:** Public (All Roles)
*   **Request Body ([ResetPasswordRequest](file:///c:/Users/dantd/OneDrive/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/auth/passwordreset/dto/ResetPasswordRequest.java)):**
    ```json
    {
      "token": "a189f38f-dc42...",
      "newPassword": "newSecurePassword123"
    }
    ```
*   **Success Response (200 OK):**
    *   *Body:* `"Password Reset Successfully"`

---

## 2. Employee Management Module
**Base Path:** `/api/v1/employees` (Requires `Authorization` header)
*   **Access Allowed Roles:** **`ADMIN`** only (checks `EMPLOYEE_MANAGE` authority)

### 2.1. Add Employee
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Request Body ([Employee](file:///c:/Users/dantd/OneDrive/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/employee/entity/Employee.java)):**
    ```json
    {
      "employeeCode": "EMP-0050",
      "name": "Jane Doe",
      "workEmail": "jane.doe@teamops.com", // Unique
      "personalEmail": "jane.doe.personal@gmail.com",
      "phone": "+91 99888 77665",
      "designation": "Software Engineer",
      "joiningDate": "2026-06-09", // Format: YYYY-MM-DD
      "status": "ACTIVE", // Allowed: "ACTIVE", "INACTIVE", "ON_LEAVE" (Defaults to "ACTIVE")
      "notificationPreference": "ALL", // Allowed: "EMAIL", "WHATSAPP", "TEAMS", "ALL" (Defaults to "ALL")
      "profileImage": "https://example.com/avatar.jpg", // Optional
      "roles": ["Employee"], // Optional: Defaults to ["Employee"]
      "user": {
        "password": "temporaryPassword123" // Required: Plain text password for the new login account
      }
    }
    ```
*   **Success Response (200 OK):** Returns the created `Employee` object (including ID, timestamps, audit details, and nested login `user` object).

### 2.2. Get All Employees
*   **HTTP Method:** `GET`
*   **Path:** `/`
*   **Request Body:** None
*   **Success Response (200 OK):** Returns a list of all active (non-deleted) `Employee` JSON objects.

### 2.3. Get Employee By ID
*   **HTTP Method:** `GET`
*   **Path:** `/{id}`
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the employee.
*   **Success Response (200 OK):** Returns the corresponding `Employee` JSON object.
*   **Error Response (404 Not Found):** Returned if the employee does not exist or has been soft-deleted.

### 2.4. Update Employee
*   **HTTP Method:** `PUT`
*   **Path:** `/{id}`
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the employee.
*   **Request Body (All fields optional):**
    ```json
    {
      "employeeCode": "EMP-0050-Updated",
      "name": "Jane Doe Smith",
      "workEmail": "jane.smith@teamops.com", // If updated, changes their login email. Must be unique.
      "personalEmail": "jane.smith.personal@gmail.com",
      "phone": "+91 99888 77665",
      "designation": "Senior Software Engineer",
      "joiningDate": "2026-06-09",
      "status": "ON_LEAVE", // If changed to "INACTIVE", deactivates their login account
      "notificationPreference": "EMAIL",
      "profileImage": "https://example.com/new-avatar.jpg",
      "roles": ["Employee", "Team Lead"], // Replaces current roles
      "user": {
        "password": "newSecurePassword123" // Optional: Updates the account's password if provided
      }
    }
    ```
*   **Success Response (200 OK):** Returns the updated `Employee` JSON object.

### 2.5. Delete Employee (Soft Delete)
*   **HTTP Method:** `DELETE`
*   **Path:** `/{id}`
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the employee to soft delete.
*   **Request Body (`text/plain`):**
    *   Send a raw text string representing the deletion reason. E.g., `"Voluntary Resignation"`
*   **Success Response (204 No Content):**
    *   *Headers:* `Status: 204 No Content`
    *   *Body:* `"Employee Deleted"`

### 2.6. Get Teams by Employee ID
*   **HTTP Method:** `GET`
*   **Path:** `/{id}/teams` (Full path: `/api/v1/employees/{id}/teams`)
*   **Access Allowed Roles:** **`ADMIN`** and **`TEAM_LEAD`** (checks `EMPLOYEE_MANAGE` or `TEAM_MANAGE` authority)
*   **Description:** Retrieves a list of all active teams that a specific employee belongs to.
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the employee.
*   **Request Payload:** None
*   **Success Response (200 OK):** Returns a JSON Array of [Team](file:///c:/Users/dantd/OneDrive/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/team/entity/Team.java) objects:
    ```json
    [
      {
        "id": 1,
        "teamName": "Engineering Core",
        "description": "Handles infrastructure and core microservices",
        "teamsChannelId": "19:abc123xyz@thread.v2",
        "status": "ACTIVE",
        "lead": {
          "id": 1,
          "name": "Alex Rivera",
          "workEmail": "alex@company.com",
          ...
        },
        "subLead": {
          "id": 3,
          "name": "Sarah Smith",
          "workEmail": "sarah@company.com",
          ...
        },
        "createdAt": "2026-06-09T17:00:00",
        "createdBy": 1,
        "updatedAt": "2026-06-09T17:00:00",
        "updatedBy": null,
        "deletedAt": null,
        "deletedBy": null,
        "deleteReason": null
      }
    ]
    ```
*   **Error Response (404 Not Found):** Returned if the employee ID is invalid or does not exist.

### 2.7. Get Projects by Employee ID
*   **HTTP Method:** `GET`
*   **Path:** `/{id}/projects` (Full path: `/api/v1/employees/{id}/projects`)
*   **Access Allowed Roles:** **`ADMIN`** and **`TEAM_LEAD`** (checks `EMPLOYEE_MANAGE` or `PROJECT_MANAGE` authority)
*   **Description:** Retrieves a list of all projects a specific employee is assigned to.
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the employee.
*   **Request Payload:** None
*   **Success Response (200 OK):** Returns a JSON Array of [Project](file:///c:/Users/dantd/OneDrive/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/project/entity/Project.java) objects:
    ```json
    [
      {
        "id": 1,
        "projectName": "Elite Portal",
        "description": "Enterprise Employee Resource Management",
        "clientName": "Acme Corp",
        "colorHex": "#8ECAE6",
        "startDate": "2026-06-10",
        "endDate": "2026-12-31",
        "status": "ACTIVE",
        "progressPercentage": 0,
        "createdAt": "2026-06-09T17:00:00",
        "createdBy": 1,
        "updatedAt": "2026-06-09T17:00:00",
        "updatedBy": null,
        "deletedAt": null,
        "deletedBy": null,
        "deleteReason": null
      }
    ]
    ```
*   **Error Response (404 Not Found):** Returned if the employee ID is invalid or does not exist.

---

## 3. Teams Module
**Base Path:** `/api/v1/teams` (Requires `Authorization` header)
*   **Access Allowed Roles:** **`ADMIN`** and **`TEAM_LEAD`** (checks `TEAM_MANAGE` authority)

### 3.1. Create Team
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Request Body ([Team](file:///c:/Users/dantd/OneDrive/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/team/entity/Team.java)):**
    ```json
    {
      "teamName": "Engineering Core", // Unique
      "description": "Handles infrastructure and core microservices", // Optional
      "teamsChannelId": "19:abc123xyz@thread.v2", // Optional Microsoft Teams channel ID
      "status": "ACTIVE", // Optional. Allowed: "ACTIVE", "INACTIVE" (Defaults to "ACTIVE")
      "lead": {
        "id": 1 // Required: Employee ID of the Team Lead
      },
      "subLead": {
        "id": 3 // Required: Employee ID of the Sub Lead (Must not be same as Lead)
      }
    }
    ```
*   **Success Response (201 Created):** Returns the created `Team` JSON object.

### 3.2. Get All Teams
*   **HTTP Method:** `GET`
*   **Path:** `/`
*   **Success Response (200 OK):** Returns a list of all active (non-deleted) `Team` objects.

### 3.3. Get Team By ID
*   **HTTP Method:** `GET`
*   **Path:** `/{id}`
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the team.
*   **Success Response (200 OK):** Returns the corresponding `Team` object.

### 3.4. Update Team
*   **HTTP Method:** `PUT`
*   **Path:** `/{id}`
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the team.
*   **Request Body (All fields optional):**
    ```json
    {
      "teamName": "Engineering Platform Core",
      "description": "Upgraded scope to manage developer platforms",
      "teamsChannelId": "19:newchannelabc@thread.v2",
      "status": "ACTIVE",
      "lead": {
        "id": 1
      },
      "subLead": {
        "id": 4 // Change sub-lead. Must differ from lead.
      }
    }
    ```
*   **Success Response (200 OK):** Returns the updated `Team` object.

### 3.5. Delete Team (Soft Delete)
*   **HTTP Method:** `DELETE`
*   **Path:** `/{id}`
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the team.
*   **Request Body (`text/plain`):**
    *   Send a raw text string representing the deletion reason. E.g., `"Team disbanded due to reorg"`
*   **Success Response (200 OK):**
    *   *Body:* `"Team Deleted"`

---

## 4. Team Members Module (Junction)
**Base Path:** `/api/v1/teams` (Requires `Authorization` header)
*   **Access Allowed Roles:** **`ADMIN`** and **`TEAM_LEAD`** (checks `TEAM_MANAGE` authority)

### 4.1. Add Employee to Team
*   **HTTP Method:** `POST`
*   **Path:** `/{teamId}/employees/{employeeId}`
*   **Path Parameters:**
    *   `teamId` (Long, Required): Database ID of the target team.
    *   `employeeId` (Long, Required): Database ID of the employee to join the team.
*   **Success Response (201 Created):** Returns the created [TeamEmployee](file:///c:/Users/dantd/OneDrive/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/team/entity/TeamEmployee.java) relationship object:
    ```json
    {
      "id": 1,
      "team": {
        "id": 1,
        "teamName": "Engineering Core",
        ...
      },
      "employee": {
        "id": 2,
        "name": "Jane Doe",
        ...
      },
      "joinedAt": "2026-06-09T17:00:00"
    }
    ```
*   **Error Responses:**
    *   `404 Not Found`: If the team or employee ID is invalid.
    *   `409 Conflict`: If the employee is already mapped to the team.

### 4.2. Remove Employee from Team
*   **HTTP Method:** `DELETE`
*   **Path:** `/{teamId}/employees/{employeeId}`
*   **Path Parameters:**
    *   `teamId` (Long, Required): Database ID of the team.
    *   `employeeId` (Long, Required): Database ID of the employee to remove.
*   **Success Response (200 OK):**
    *   *Body:* `"Employee removed from team"`
*   **Error Response (404 Not Found):** If the employee was not mapped to this team.

### 4.3. Get All Team Members
*   **HTTP Method:** `GET`
*   **Path:** `/{teamId}/employees`
*   **Path Parameters:**
    *   `teamId` (Long, Required): Database ID of the team.
*   **Success Response (200 OK):** Returns a list of `Employee` JSON objects belonging to the team.

---

## 5. Project Management Module
**Base Path:** `/api/v1/projects` (Requires `Authorization` header)
*   **Access Allowed Roles:** **`ADMIN`** and **`TEAM_LEAD`** (checks `PROJECT_MANAGE` authority)

### 5.1. Add Project
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Request Body ([Project](file:///c:/Users/dantd/OneDrive/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/project/entity/Project.java)):**
    ```json
    {
      "projectName": "Elite Portal",
      "description": "Enterprise Employee Resource Management",
      "clientName": "Acme Corp",
      "colorHex": "#8ECAE6",
      "startDate": "2026-06-10",
      "endDate": "2026-12-31",
      "status": "ACTIVE",
      "progressPercentage": 0
    }
    ```
*   **Success Response (201 Created):** Returns the created `Project` JSON object.

### 5.2. Get All Projects
*   **HTTP Method:** `GET`
*   **Path:** `/`
*   **Request Body:** None
*   **Success Response (200 OK):** Returns a list of all active (non-deleted) `Project` JSON objects.

### 5.3. Get Project By ID
*   **HTTP Method:** `GET`
*   **Path:** `/{id}`
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the project.
*   **Success Response (200 OK):** Returns the corresponding `Project` JSON object.
*   **Error Response (404 Not Found):** Returned if the project does not exist or has been soft-deleted.

### 5.4. Update Project
*   **HTTP Method:** `PUT`
*   **Path:** `/{id}`
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the project.
*   **Request Body (All fields optional):**
    ```json
    {
      "projectName": "Elite Portal v2",
      "description": "Updated project description",
      "clientName": "Acme Corp International",
      "colorHex": "#219EBC",
      "startDate": "2026-06-11",
      "endDate": "2027-01-31",
      "status": "ON_HOLD",
      "progressPercentage": 25
    }
    ```
*   **Success Response (200 OK):** Returns the updated `Project` JSON object.

### 5.5. Delete Project (Soft Delete)
*   **HTTP Method:** `DELETE`
*   **Path:** `/{id}`
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the project to soft delete.
*   **Request Body (`text/plain`):**
    *   Send a raw text string representing the deletion reason. E.g., `"Project cancelled by client"`
*   **Success Response (200 OK):**
    *   *Body:* `"Project Deleted"`

---

## 6. Project Members Module (Junction)
**Base Path:** `/api/v1/projects` (Requires `Authorization` header)
*   **Access Allowed Roles:** **`ADMIN`** and **`TEAM_LEAD`** (checks `PROJECT_MANAGE` authority)

### 6.1. Add Employee to Project
*   **HTTP Method:** `POST`
*   **Path:** `/{projectId}/employees/{employeeId}`
*   **Path Parameters:**
    *   `projectId` (Long, Required): Database ID of the target project.
    *   `employeeId` (Long, Required): Database ID of the employee to join the project.
*   **Success Response (201 Created):** Returns the created [ProjectEmployee](file:///c:/Users/dantd/OneDrive/Desktop/employeemanager-elite/src/main/java/com/elite/employeemanager/project/entity/ProjectEmployee.java) relationship object:
    ```json
    {
      "id": 1,
      "project": {
        "id": 1,
        "projectName": "Elite Portal",
        "description": "Enterprise Employee Resource Management",
        "clientName": "Acme Corp",
        "colorHex": "#8ECAE6",
        "startDate": "2026-06-10",
        "endDate": "2026-12-31",
        "status": "ACTIVE",
        "progressPercentage": 0
      },
      "employee": {
        "id": 2,
        "name": "Jane Doe",
        "workEmail": "jane.doe@teamops.com",
        "personalEmail": "jane.doe.personal@gmail.com",
        "phone": "+91 99888 77665",
        "designation": "Software Engineer",
        "joiningDate": "2026-06-09",
        "status": "ACTIVE",
        "notificationPreference": "ALL",
        "profileImage": "https://example.com/avatar.jpg"
      }
    }
    ```
*   **Error Responses:**
    *   `404 Not Found`: If the project or employee ID is invalid/does not exist.
    *   `400 Bad Request`: If the employee is already mapped to the project.

### 6.2. Remove Employee from Project
*   **HTTP Method:** `DELETE`
*   **Path:** `/{projectId}/employees/{employeeId}`
*   **Path Parameters:**
    *   `projectId` (Long, Required): Database ID of the project.
    *   `employeeId` (Long, Required): Database ID of the employee to remove.
*   **Success Response (200 OK):**
    *   *Body:* `"Employee Removed from Project"`
*   **Error Response (404 Not Found):** If the employee was not mapped to this project or either ID is invalid.

### 6.3. Get All Project Members
*   **HTTP Method:** `GET`
*   **Path:** `/{projectId}/employees`
*   **Path Parameters:**
    *   `projectId` (Long, Required): Database ID of the project.
*   **Success Response (200 OK):** Returns a list of `Employee` JSON objects belonging to the project.
