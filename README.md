
# Steps:

## 1. run psql shell command :

CREATE DATABASE empmanelite;

## 2. set environment variables :

LOCAL_DB_USERNAME = your_postgres_user\
LOCAL_DB_PASSWORD = your_postgres_password\
JWT_SECRET = your_hs512_hex_jwt_secret\
MAIL_USERNAME = your_email_username_here\
MAIL_PASSWORD = your_email_password_here\
FRONTEND_URL = your_frontend_url\
AWS_REGION = your_aws_region\
AWS_BUCKET_NAME = your_aws_s3_bucket_name\
AWS_ACCESS_KEY_ID = your_aws_access_key_id\
AWS_SECRET_ACCESS_KEY = your_aws_secret_access_key\
TEAMS_WEBHOOK_URL = your_ms_teams_workflow_webhook_url


## 3. run app :

/src/main/java/com/elite/employeemanager/EmployeemanagerApplication.java

---

# API (v1)

## Base Configuration
*   **Base URL:** `http://localhost:8080/api/v1`
*   **Default Headers:**
    *   `Content-Type: application/json`
*   **Authentication:**
    *   Protected endpoints require HTTPOnly cookies: `jwtToken` (JWT Access Token) and `refreshToken` (Refresh Token UUID).

## Role-Based Access Mapping
Access to protected endpoints is governed by authorities compiled from user roles on login:

| Role Code | Role Name | Granted Authorities / Permissions | Allowed Modules / Endpoints |
| :--- | :--- | :--- | :--- |
| **`ADMIN`** | Admin | `EMPLOYEE_CREATE`, `EMPLOYEE_VIEW`, `EMPLOYEE_UPDATE`, `EMPLOYEE_DELETE`, `TEAM_CREATE`, `TEAM_VIEW`, `TEAM_UPDATE`, `TEAM_DELETE`, `PROJECT_CREATE`, `PROJECT_VIEW`, `PROJECT_UPDATE`, `PROJECT_DELETE`, `TASK_CREATE`, `TASK_VIEW`, `TASK_UPDATE`, `TASK_DELETE`, `USER_CREATE`, `USER_VIEW`, `USER_UPDATE`, `USER_DELETE`, `MEETING_VIEW`, `MEETING_CREATE`, `MEETING_UPDATE`, `MEETING_DELETE` | **All Endpoints** (Authentication, Employees, Teams, Projects, Tasks, Comments, Progress, Tags, Attachments, ETA requests, Task transfers, Meetings) |
| **`TEAM_LEAD`** | Team Lead | `TEAM_CREATE`, `TEAM_VIEW`, `TEAM_UPDATE`, `TEAM_DELETE`, `PROJECT_CREATE`, `PROJECT_VIEW`, `PROJECT_UPDATE`, `PROJECT_DELETE`, `TASK_CREATE`, `TASK_VIEW`, `TASK_UPDATE`, `TASK_DELETE`, `MEETING_VIEW`, `MEETING_CREATE`, `MEETING_UPDATE`, `MEETING_DELETE` | Authentication, Teams, Projects, Tasks, Comments, Progress, Tags, Attachments, ETA requests, Task transfers, Meetings (No Employee Management) |
| **`SUB_LEAD`** | Sub Lead | `TASK_VIEW`, `MEETING_VIEW`, `MEETING_CREATE`, `MEETING_UPDATE`, `MEETING_DELETE` | Authentication, Tasks (Assigned), Comments, Progress, Attachments, ETA/Transfer requests, Meetings (No Employee/Team/Project management unless assigned manually) |
| **`EMPLOYEE`** | Employee | `TASK_VIEW`, `MEETING_VIEW`, `MEETING_CREATE`, `MEETING_UPDATE`, `MEETING_DELETE` | Authentication, Tasks (Assigned), Comments, Progress, Attachments, ETA/Transfer requests, Meetings |

---

## 1. Authentication Module
**Base Path:** `/api/v1/auth` (Publicly accessible — no token required)

### 1.1. User Login
*   **HTTP Method:** `POST`
*   **Path:** `/login`
*   **Access Allowed:** Public (All Roles)
*   **Request Body ([LoginRequest](./src/main/java/com/elite/employeemanager/auth/jwt/dto/LoginRequest.java)):**
    ```json
    {
      "email": "employee@teamops.com",
      "password": "employee123"
    }
    ```
*   **Cookies Set (Set-Cookie Response Headers):**
    *   `jwtToken`: HTTPOnly, sameSite="Lax", path="/" (containing JWT Access Token)
    *   `refreshToken`: HTTPOnly, sameSite="Lax", path="/" (containing Refresh Token UUID)
*   **Success Response (200 OK - [AuthenticationResponse](./src/main/java/com/elite/employeemanager/auth/jwt/dto/AuthenticationResponse.java)):**
    ```json
    {
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
*   **Access Allowed:** Public (All Roles, reads the `refreshToken` HTTPOnly Cookie)
*   **Request Body:** None
*   **Cookies Set (Set-Cookie Response Header):**
    *   `jwtToken`: HTTPOnly, sameSite="Lax", path="/" (updated JWT Access Token)
*   **Success Response (200 OK):**
    *   Returns updated user information in `AuthenticationResponse` format (same format as `POST /login`).

### 1.3. User Logout
*   **HTTP Method:** `POST`
*   **Path:** `/logout`
*   **Access Allowed:** Public (All Roles, deletes the refresh token from database)
*   **Request Body:** None
*   **Cookies Set (Set-Cookie Response Headers):**
    *   Clears both `jwtToken` and `refreshToken` (sets `maxAge` to `0`).
*   **Success Response (200 OK):**
    *   *Body:* `"Logged out"`

### 1.4. Forgot Password
*   **HTTP Method:** `POST`
*   **Path:** `/forgot-password`
*   **Access Allowed:** Public (All Roles)
*   **Request Body ([ForgotPasswordRequest](./src/main/java/com/elite/employeemanager/auth/passwordreset/dto/ForgotPasswordRequest.java)):**
    ```json
    {
      "email": "employee@teamops.com"
    }
    ```
*   **Success Response (200 OK):**
    *   *Body:* `"If the email exists, a password reset link has been sent."`

### 1.5. Reset Password
*   **HTTP Method:** `POST`
*   **Path:** `/reset-password`
*   **Access Allowed:** Public (All Roles)
*   **Request Body ([ResetPasswordRequest](./src/main/java/com/elite/employeemanager/auth/passwordreset/dto/ResetPasswordRequest.java)):**
    ```json
    {
      "token": "a189f38f-dc42...",
      "newPassword": "newSecurePassword123"
    }
    ```
*   **Success Response (200 OK):**
    *   *Body:* `"Password Reset Successfully"`

-----

## 2. Employee Management Module
**Base Path:** `/api/v1/employees` (Requires HTTPOnly cookies)
*   **Access Allowed:** Checked via `EMPLOYEE_CREATE`, `EMPLOYEE_VIEW`, `EMPLOYEE_UPDATE`, `EMPLOYEE_DELETE` permissions (typically granted to `ADMIN`)

### 2.1. Add Employee
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Request Body ([Employee](./src/main/java/com/elite/employeemanager/employee/entity/Employee.java)):**
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
*   **Access Allowed:** Checks `EMPLOYEE_VIEW` or `TEAM_VIEW` permission
*   **Description:** Retrieves a list of all active teams that a specific employee belongs to.
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the employee.
*   **Request Payload:** None
*   **Success Response (200 OK):** Returns a JSON Array of [Team](./src/main/java/com/elite/employeemanager/team/entity/Team.java) objects:
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
*   **Access Allowed:** Checks `EMPLOYEE_VIEW` or `PROJECT_VIEW` permission
*   **Description:** Retrieves a list of all projects a specific employee is assigned to.
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the employee.
*   **Request Payload:** None
*   **Success Response (200 OK):** Returns a JSON Array of [Project](./src/main/java/com/elite/employeemanager/project/entity/Project.java) objects:
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

### 2.8. Get Tasks by Employee ID
*   **HTTP Method:** `GET`
*   **Path:** `/{id}/tasks` (Full path: `/api/v1/employees/{id}/tasks`)
*   **Access Allowed:** Checks `EMPLOYEE_VIEW` or `TASK_VIEW` permission
*   **Description:** Retrieves a list of all tasks assigned to a specific employee.
*   **Path Parameters:**
    *   `id` (Long, Required): Database ID of the employee.
*   **Request Payload:** None
*   **Success Response (200 OK):** Returns a JSON Array of [Task](./src/main/java/com/elite/employeemanager/task/entity/Task.java) objects:
    ```json
    [
      {
        "id": 1,
        "taskNumber": "TSK-0001",
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
        "title": "Setup database indexes",
        "description": "Create partial indexes for soft-delete support",
        "taskType": "TASK",
        "priority": "HIGH",
        "status": "IN_PROGRESS",
        "etaHours": 8.00,
        "etaDate": "2026-06-15",
        "originalEtaDate": "2026-06-15",
        "extendedEtaDate": null,
        "bugNumber": null,
        "assignedTo": {
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
        },
        "epic": "Sprint 1",
        "createdAt": "2026-06-11T12:00:00",
        "createdBy": 1,
        "updatedAt": "2026-06-11T12:00:00",
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
**Base Path:** `/api/v1/teams` (Requires HTTPOnly cookies)
*   **Access Allowed:** Checked via `TEAM_CREATE`, `TEAM_VIEW`, `TEAM_UPDATE`, `TEAM_DELETE` permissions (typically granted to `ADMIN` and `TEAM_LEAD`)

### 3.1. Create Team
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Request Body ([Team](./src/main/java/com/elite/employeemanager/team/entity/Team.java)):**
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
**Base Path:** `/api/v1/teams` (Requires HTTPOnly cookies)
*   **Access Allowed:** Checks `TEAM_UPDATE` permission

### 4.1. Add Employee to Team
*   **HTTP Method:** `POST`
*   **Path:** `/{teamId}/employees/{employeeId}`
*   **Path Parameters:**
    *   `teamId` (Long, Required): Database ID of the target team.
    *   `employeeId` (Long, Required): Database ID of the employee to join the team.
*   **Success Response (201 Created):** Returns the created [TeamEmployee](./src/main/java/com/elite/employeemanager/team/entity/TeamEmployee.java) relationship object:
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
**Base Path:** `/api/v1/projects` (Requires HTTPOnly cookies)
*   **Access Allowed:** Checked via `PROJECT_CREATE`, `PROJECT_VIEW`, `PROJECT_UPDATE`, `PROJECT_DELETE` permissions (typically granted to `ADMIN` and `TEAM_LEAD`)

### 5.1. Add Project
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Request Body ([Project](./src/main/java/com/elite/employeemanager/project/entity/Project.java)):**
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
**Base Path:** `/api/v1/projects` (Requires HTTPOnly cookies)
*   **Access Allowed:** Checks `PROJECT_UPDATE` or `PROJECT_VIEW` permission

### 6.1. Add Employee to Project
*   **HTTP Method:** `POST`
*   **Path:** `/{projectId}/employees/{employeeId}`
*   **Path Parameters:**
    *   `projectId` (Long, Required): Database ID of the target project.
    *   `employeeId` (Long, Required): Database ID of the employee to join the project.
*   **Success Response (201 Created):** Returns the created [ProjectEmployee](./src/main/java/com/elite/employeemanager/project/entity/ProjectEmployee.java) relationship object:
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

---

## 7. Task Management Module
**Base Path:** `/api/v1/tasks` (Requires HTTPOnly cookies)

### 7.1. Add Task
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Request Body ([Task](./src/main/java/com/elite/employeemanager/task/entity/Task.java)):**
    ```json
    {
      "taskNumber": "TSK-0002",
      "project": {
        "id": 1
      },
      "title": "Implement AWS S3 file upload",
      "description": "Create service and controller to handle attachments",
      "taskType": "FEATURE", // Allowed: "FEATURE", "BUG", "STORY", "RND", "CRC", "COC", "SUPPORT", "TASK", "POC"
      "priority": "HIGH", // Allowed: "LOW", "MEDIUM", "HIGH", "CRITICAL"
      "status": "OPEN", // Allowed: "OPEN", "IN_PROGRESS", "PENDING_REVIEW", "COMPLETED", "OVER_ETA", "TRANSFERRED", "ETA_EXTENDED", "REJECTED"
      "etaHours": 12.50,
      "etaDate": "2026-06-20", // Format: YYYY-MM-DD
      "bugNumber": null, // Required if taskType is "BUG"
      "assignedTo": {
        "id": 2
      },
      "epic": "Sprint 2"
    }
    ```
*   **Validation Rules:**
    *   `taskNumber`, `title`, `project.id`, `taskType`, `priority`, `etaHours`, `etaDate` are required.
    *   `taskNumber` must be unique.
    *   If `taskType` is `"BUG"`, `bugNumber` is required.
    *   If `assignedTo` is provided, the employee must belong to the specified project.
*   **Success Response (201 Created):** Returns the created `Task` object.

### 7.2. Get All Tasks
*   **HTTP Method:** `GET`
*   **Path:** `/`
*   **Success Response (200 OK):** Returns a list of all `Task` objects.

### 7.3. Get Task By ID
*   **HTTP Method:** `GET`
*   **Path:** `/{id}`
*   **Success Response (200 OK):** Returns the corresponding `Task` object.

### 7.4. Update Task (Partial Update)
*   **HTTP Method:** `PATCH`
*   **Path:** `/{id}`
*   **Request Body (All fields optional):**
    ```json
    {
      "title": "Implement AWS S3 file upload - Updated",
      "status": "IN_PROGRESS",
      "etaHours": 14.00
    }
    ```
*   **Success Response (200 OK):** Returns the updated `Task` object. Automatically records audit history if status changes.

### 7.5. Delete Task (Soft Delete)
*   **HTTP Method:** `DELETE`
*   **Path:** `/{id}`
*   **Request Body (`text/plain`):** Raw string representing deletion reason. E.g. `"No longer needed"`
*   **Success Response (200 OK):** `"Task Deleted Successfully"`

### 7.6. Unassign Task
*   **HTTP Method:** `PATCH`
*   **Path:** `/{id}/unassign`
*   **Success Response (200 OK):** `"Task unassigned"` (Deletes any pending ETA requests for this task).

### 7.7. Get Task Comments
*   **HTTP Method:** `GET`
*   **Path:** `/{id}/comments`
*   **Success Response (200 OK):** Returns a list of [TaskComment](./src/main/java/com/elite/employeemanager/task/entity/TaskComment.java) objects associated with the task.

### 7.8. Get Task Status History
*   **HTTP Method:** `GET`
*   **Path:** `/{id}/history`
*   **Success Response (200 OK):** Returns a list of [TaskStatusHistory](./src/main/java/com/elite/employeemanager/task/entity/TaskStatusHistory.java) objects showing status transition audit logs.

### 7.9. Get Task Progress Logs
*   **HTTP Method:** `GET`
*   **Path:** `/{id}/progress`
*   **Success Response (200 OK):** Returns a list of [TaskProgress](./src/main/java/com/elite/employeemanager/task/entity/TaskProgress.java) logs logged for this task.

### 7.10. Get Task Tags
*   **HTTP Method:** `GET`
*   **Path:** `/{id}/tags`
*   **Success Response (200 OK):** Returns a list of [TaskTag](./src/main/java/com/elite/employeemanager/task/entity/TaskTag.java) objects mapped to this task.

### 7.11. Get Task Attachments
*   **HTTP Method:** `GET`
*   **Path:** `/{id}/attachments`
*   **Success Response (200 OK):** Returns a list of [TaskAttachment](./src/main/java/com/elite/employeemanager/task/entity/TaskAttachment.java) metadata objects for this task.

### 7.12. Get Task ETA Extension Requests
*   **HTTP Method:** `GET`
*   **Path:** `/{id}/eta-extensions`
*   **Success Response (200 OK):** Returns a list of [EtaExtension](./src/main/java/com/elite/employeemanager/task/entity/EtaExtension.java) requests for this task.

### 7.13. Get Task Transfer Requests
*   **HTTP Method:** `GET`
*   **Path:** `/{id}/task-transfers`
*   **Success Response (200 OK):** Returns a list of [TaskTransfer](./src/main/java/com/elite/employeemanager/task/entity/TaskTransfer.java) requests for this task.

### 7.14. Submit Task Completion Review
*   **HTTP Method:** `POST`
*   **Path:** `/{id}/submit-review`
*   **Access Allowed:** Only the assigned employee can submit.
*   **Request Body ([TaskReviewSubmitRequest](./src/main/java/com/elite/employeemanager/task/dto/TaskReviewSubmitRequest.java)):**
    ```json
    {
      "justification": "Task completed successfully, all tests passed." // Required only if task breaches ETA date/hours
    }
    ```
*   **Success Response (200 OK):** Returns the updated `Task` object (status: `"PENDING_REVIEW"`).

### 7.15. Review Task Completion
*   **HTTP Method:** `POST`
*   **Path:** `/{id}/review`
*   **Access Allowed:** Admin, Team Lead, or Sub Lead with managed team permissions.
*   **Request Body ([TaskReviewRequest](./src/main/java/com/elite/employeemanager/task/dto/TaskReviewRequest.java)):**
    ```json
    {
      "status": "APPROVED", // Allowed: "APPROVED", "REJECTED"
      "comment": "Nice work!"
    }
    ```
*   **Success Response (200 OK):** Returns the updated `Task` object. Status transitions to `"COMPLETED"` if approved, or reverts to `"IN_PROGRESS"` if rejected. Sets `completionReviewStatus` to `"APPROVED"` or `"REJECTED"`.

### 7.16. Unsubmit Task Review
*   **HTTP Method:** `POST`
*   **Path:** `/{id}/unsubmit-review`
*   **Access Allowed:** Only the assigned employee can unsubmit.
*   **Success Response (200 OK):** Returns the updated `Task` object. Reverts task status to its state before review submission (typically `IN_PROGRESS` or `OVER_ETA`) and clears `justification`.

### 7.17. Undo Task Review Decision
*   **HTTP Method:** `POST`
*   **Path:** `/{id}/undo-review`
*   **Access Allowed:** Admin, Team Lead, or Sub Lead with managed team permissions.
*   **Success Response (200 OK):** Returns the updated `Task` object. Status is set back to `"PENDING_REVIEW"` and `completionReviewStatus` and `reviewComment` are reset to `null`.

---

## 8. Task Comments Module
**Base Path:** `/api/v1/task-comments` (Requires HTTPOnly cookies)

### 8.1. Add Task Comment
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Request Body ([TaskComment](./src/main/java/com/elite/employeemanager/task/entity/TaskComment.java)):**
    ```json
    {
      "commentText": "Configured the S3 clients",
      "task": {
        "id": 1
      },
      "author": {
        "id": 2
      }
    }
    ```
*   **Success Response (201 Created):** Returns the created `TaskComment` object. The author is automatically set to the currently authenticated user.

### 8.2. Delete Task Comment
*   **HTTP Method:** `DELETE`
*   **Path:** `/{id}`
*   **Success Response (200 OK):** `"Comment Deleted Successfully"`

---

## 9. Task Progress Module
**Base Path:** `/api/v1/task-progress` (Requires HTTPOnly cookies)

### 9.1. Add Task Progress Log
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Request Body ([TaskProgress](./src/main/java/com/elite/employeemanager/task/entity/TaskProgress.java)):**
    ```json
    {
      "progressPercentage": 45,
      "notes": "Added properties and S3Client config",
      "task": {
        "id": 1
      },
      "employee": {
        "id": 2
      }
    }
    ```
*   **Validation Rules:**
    *   `progressPercentage` must be between 0 and 100.
    *   The task must be assigned to the current employee.
    *   The employee must belong to the project of this task.
*   **Success Response (201 Created):** Returns the created `TaskProgress` object.

### 9.2. Delete Task Progress Log
*   **HTTP Method:** `DELETE`
*   **Path:** `/{id}`
*   **Success Response (200 OK):** `"Task Progress Deleted"`

---

## 10. Task Tags & Tag Mapping Module
**Base Paths:** `/api/v1/task-tags`, `/api/v1/tasks` (Requires HTTPOnly cookies)

### 10.1. Create Task Tag
*   **HTTP Method:** `POST`
*   **Path:** `/api/v1/task-tags`
*   **Request Body ([TaskTag](./src/main/java/com/elite/employeemanager/task/entity/TaskTag.java)):**
    ```json
    {
      "tagName": "backend"
    }
    ```
*   **Success Response (201 Created):** Returns the created `TaskTag` object.

### 10.2. Get All Task Tags
*   **HTTP Method:** `GET`
*   **Path:** `/api/v1/task-tags`
*   **Success Response (200 OK):** Returns a list of all `TaskTag` objects.

### 10.3. Get Task Tag By ID
*   **HTTP Method:** `GET`
*   **Path:** `/api/v1/task-tags/{id}`
*   **Success Response (200 OK):** Returns the corresponding `TaskTag` object.

### 10.4. Delete Task Tag
*   **HTTP Method:** `DELETE`
*   **Path:** `/api/v1/task-tags/{id}`
*   **Success Response (200 OK):** `"Task Tag deleted successfully"`

### 10.5. Add Tag to Task
*   **HTTP Method:** `POST`
*   **Path:** `/api/v1/tasks/{taskId}/tags/{tagId}`
*   **Success Response (201 Created):** Returns the created [TaskTagMapping](./src/main/java/com/elite/employeemanager/task/entity/TaskTagMapping.java) object.

### 10.6. Remove Tag from Task
*   **HTTP Method:** `DELETE`
*   **Path:** `/api/v1/tasks/{taskId}/tags/{tagId}`
*   **Success Response (200 OK):** `"Tag removed from Task Successfully"`

---

## 11. Task Attachments Module (AWS S3)
**Base Path:** `/api/v1/tasks` (Requires HTTPOnly cookies)

### 11.1. Upload Attachment
*   **HTTP Method:** `POST`
*   **Path:** `/{taskId}/attachments`
*   **Request Headers:** `Content-Type: multipart/form-data`
*   **Request Parameters:**
    *   `file` (MultipartFile): The file to upload (Max: 50MB).
*   **Success Response (201 Created):** Returns the created [TaskAttachment](./src/main/java/com/elite/employeemanager/task/entity/TaskAttachment.java) metadata object. File is uploaded to the AWS S3 bucket.

### 11.2. Download Attachment
*   **HTTP Method:** `GET`
*   **Path:** `/{taskId}/attachments/{attachmentId}`
*   **Success Response (200 OK):** Returns the binary file stream (`application/octet-stream`) with `Content-Disposition: attachment; filename="..."`.

### 11.3. Delete Attachment
*   **HTTP Method:** `DELETE`
*   **Path:** `/{taskId}/attachments/{attachmentId}`
*   **Success Response (200 OK):** `"File Deleted Successfully"`

---

## 12. ETA Extension Requests Module
**Base Path:** `/api/v1/eta-extensions` (Requires HTTPOnly cookies)

### 12.1. Create ETA Extension Request
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Request Body ([EtaExtension](./src/main/java/com/elite/employeemanager/task/entity/EtaExtension.java)):**
    ```json
    {
      "task": {
        "id": 1
      },
      "newEtaDate": "2026-06-25",
      "reason": "Need more time to write integration tests"
    }
    ```
*   **Validation Rules:**
    *   The task must not be completed or deleted.
    *   The task must be assigned to the current employee.
    *   `newEtaDate` cannot be on or before the task's current ETA date.
    *   The task must not already have a pending ETA request.
*   **Success Response (201 Created):** Returns the created `EtaExtension` object (initial status: `"PENDING"`).

### 12.2. Get ETA Extension Request By ID
*   **HTTP Method:** `GET`
*   **Path:** `/{id}`
*   **Success Response (200 OK):** Returns the corresponding `EtaExtension` request object.

### 12.3. Approve ETA Extension Request
*   **HTTP Method:** `PATCH`
*   **Path:** `/{id}/approve`
*   **Success Response (200 OK):** Returns the updated `EtaExtension` object (status: `"APPROVED"`). Updates the task's `etaDate` and `extendedEtaDate` to the requested new date.

### 12.4. Reject ETA Extension Request
*   **HTTP Method:** `PATCH`
*   **Path:** `/{id}/reject`
*   **Request Body (`text/plain`):** Raw string representing rejection reason. E.g. `"Not justified"`
*   **Success Response (200 OK):** Returns the updated `EtaExtension` object (status: `"REJECTED"`, sets `rejectionReason`).

### 12.5. Undo ETA Extension Request Decision
*   **HTTP Method:** `PATCH`
*   **Path:** `/{id}/undo`
*   **Success Response (200 OK):** Returns the updated `EtaExtension` object (status reset to `"PENDING"`). Reverts the task's `etaDate` and `extendedEtaDate` if the decision was previously approved.

---

## 13. Task Transfer Requests Module
**Base Path:** `/api/v1/task-transfers` (Requires HTTPOnly cookies)

### 13.1. Create Task Transfer Request
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Request Body ([TaskTransfer](./src/main/java/com/elite/employeemanager/task/entity/TaskTransfer.java)):**
    ```json
    {
      "task": {
        "id": 1
      },
      "targetEmployee": {
        "id": 3
      },
      "reason": "Going on leave next week, transferring to coworker"
    }
    ```
*   **Validation Rules:**
    *   The task must not be completed or deleted.
    *   The task must be assigned to the current employee.
    *   The target employee must not be deleted/inactive, must belong to the task's project, and must not be the current assignee.
    *   The task must not already have a pending transfer request.
*   **Success Response (201 Created):** Returns the created `TaskTransfer` object (initial status: `"PENDING"`).

### 13.2. Get Task Transfer Request By ID
*   **HTTP Method:** `GET`
*   **Path:** `/{id}`
*   **Success Response (200 OK):** Returns the corresponding `TaskTransfer` request object.

### 13.3. Approve Task Transfer Request
*   **HTTP Method:** `PATCH`
*   **Path:** `/{id}/approve`
*   **Success Response (200 OK):** Returns the updated `TaskTransfer` object (status: `"APPROVED"`). Reassigns the task to the target employee and deletes any pending ETA extension requests for this task.

### 13.4. Reject Task Transfer Request
*   **HTTP Method:** `PATCH`
*   **Path:** `/{id}/reject`
*   **Request Body (`text/plain`):** Raw string representing rejection reason. E.g. `"Target employee has too many tasks already"`
*   **Success Response (200 OK):** Returns the updated `TaskTransfer` object (status: `"REJECTED"`, sets `rejectionReason`).

### 13.5. Undo Task Transfer Request Decision
*   **HTTP Method:** `PATCH`
*   **Path:** `/{id}/undo`
*   **Success Response (200 OK):** Returns the updated `TaskTransfer` object (status reset to `"PENDING"`). Reassigns the task back to the original requester if the decision was previously approved.

---

## 14. Timesheet Management Module
**Base Path:** `/api/v1/timesheets` (Requires HTTPOnly cookies)

### 14.1. Get All Timesheet Entries
*   **HTTP Method:** `GET`
*   **Path:** `/`
*   **Query Parameters (All optional):**
    *   `employeeId` (Long): Target employee database ID.
    *   `date` (LocalDate): Specific date to filter (Format: `YYYY-MM-DD`).
    *   `status` (String): Filter status (e.g. `"APPROVED"`, `"REJECTED"`, `"PENDING"`).
*   **Success Response (200 OK):** Returns a list of timesheet entry responses:
    ```json
    [
      {
        "id": 1,
        "employeeId": 2,
        "employeeName": "Jane Doe",
        "task": {
          "id": 1,
          "taskNumber": "TSK-0001",
          "title": "Setup database indexes"
        },
        "project": {
          "id": 1,
          "projectName": "Elite Portal"
        },
        "bugNumber": null,
        "workCategory": "TASK",
        "date": "2026-06-15",
        "startTime": "2026-06-15T09:00:00",
        "endTime": "2026-06-15T17:00:00",
        "durationHours": 8.00,
        "description": "Implemented indexes on task status history table",
        "justification": null,
        "status": "APPROVED",
        "managerComment": null,
        "approvedBy": "Alex Rivera",
        "approvedAt": "2026-06-15T18:00:00"
      }
    ]
    ```

### 14.2. Create Timesheet Entry
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Request Body ([TimesheetRequest](./src/main/java/com/elite/employeemanager/timesheet/dto/TimesheetRequest.java)):**
    ```json
    {
      "employee": { "id": 2 },
      "task": { "id": 1 },
      "project": { "id": 1 },
      "bugNumber": null,
      "workCategory": "TASK", // Allowed: "TASK", "BREAK", "MEETING", "SUPPORT", "LEARNING", "OTHER"
      "date": "2026-06-15",
      "startTime": "2026-06-15T09:00:00",
      "endTime": "2026-06-15T17:00:00",
      "durationHours": 8.00,
      "description": "Implemented indexes on task status history table",
      "justification": null
    }
    ```
*   **Validation Rules:**
    *   `employee.id`, `workCategory`, `startTime`, `endTime`, `date`, `description` are required.
    *   End time must be after start time.
    *   If `durationHours` is provided, it must exactly match the calculated difference between start and end times.
    *   Logged logs must not overlap with any existing entries on the same date for that employee.
    *   If not a break, must link to at least one reference (task, project, or bug).
    *   Cannot log against tasks that are in `PENDING_REVIEW` or `COMPLETED` status.
*   **Success Response (201 Created):** Returns the created timesheet entry JSON response.

### 14.3. Update Timesheet Entry Status (Approval/Rejection)
*   **HTTP Method:** `PATCH`
*   **Path:** `/{id}/status`
*   **Request Body ([TimesheetStatusUpdateRequest](./src/main/java/com/elite/employeemanager/timesheet/dto/TimesheetStatusUpdateRequest.java)):**
    ```json
    {
      "status": "APPROVED", // Allowed: "APPROVED", "REJECTED"
      "managerComment": "Approved. Good job."
    }
    ```
*   **Success Response (200 OK):** Returns the updated timesheet entry JSON response.

### 14.4. Patch Update Timesheet Entry
*   **HTTP Method:** `PATCH`
*   **Path:** `/{id}`
*   **Request Body (All fields optional):**
    ```json
    {
      "startTime": "2026-06-15T10:00:00",
      "endTime": "2026-06-15T17:00:00",
      "durationHours": 7.00,
      "description": "Updated hours log description"
    }
    ```
*   **Validation Rules:**
    *   Applies the same overlap, duration matching, project membership, and task status validations as entry creation.
*   **Success Response (200 OK):** Returns the updated timesheet entry JSON response.

### 14.5. Delete Timesheet Entry
*   **HTTP Method:** `DELETE`
*   **Path:** `/{id}`
*   **Success Response (200 OK):** `"Timesheet Entry deleted successfully"`

---

## 15. Attachment Management Module
**Base Path:** `/api/v1/attachments` (Requires HTTPOnly cookies)

### 15.1. Upload Attachment
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Content-Type:** `multipart/form-data`
*   **Query Param:** `meetingId` — ID of the meeting to link (required)
*   **Form Param:** `file` — the file to upload (max 50 MB)
*   **Required Permission:** `ATTACHMENT_UPLOAD`
*   **Success Response (201 Created):** Returns the saved `Attachment` object (id, fileName, filePath, fileSizeBytes, uploadedBy, uploadedAt, meeting).

### 15.2. Get All Attachments
*   **HTTP Method:** `GET`
*   **Path:** `/`
*   **Required Permission:** `ATTACHMENT_VIEW`
*   **Success Response (200 OK):** Returns a list of all `Attachment` objects.

### 15.3. Get Attachment Metadata By ID
*   **HTTP Method:** `GET`
*   **Path:** `/{id}`
*   **Required Permission:** `ATTACHMENT_VIEW`
*   **Success Response (200 OK):** Returns the `Attachment` metadata object.
*   **Error Response (404 Not Found):** `"Attachment Not Found"`

### 15.4. Download Attachment
*   **HTTP Method:** `GET`
*   **Path:** `/{id}/download`
*   **Required Permission:** `ATTACHMENT_VIEW`
*   **Success Response (200 OK):** Streams the file content from S3 with:
    *   `Content-Disposition: attachment; filename="<originalFileName>"`
    *   `Content-Type` set to the file's MIME type.
    *   `Content-Length` set from the S3 object metadata.
*   **Error Response (404 Not Found):** File not found in S3 or DB.

### 15.5. Delete Attachment
*   **HTTP Method:** `DELETE`
*   **Path:** `/{id}`
*   **Required Permission:** `ATTACHMENT_DELETE`
*   **Success Response (200 OK):** `"Attachment deleted successfully"`
*   **Error Response (403 Forbidden):** If the caller is not the uploader, an admin, or the uploader's team lead/sub-lead.
*   **Notes:** Deletes from both S3 storage and the database atomically.

---

## 16. Feed & Microsoft Teams Integration Module
**Base Path:** `/api/v1/feed` (Requires HTTPOnly cookies)

### 16.1. Get All Announcements
*   **HTTP Method:** `GET`
*   **Path:** `/`
*   **Required Permission:** `ANNOUNCEMENT_VIEW`
*   **Success Response (200 OK):** Returns a list of all announcements/feed entries.

### 16.2. Get Announcement By ID
*   **HTTP Method:** `GET`
*   **Path:** `/{id}`
*   **Required Permission:** `ANNOUNCEMENT_VIEW`
*   **Success Response (200 OK):** Returns the requested announcement metadata and status.
*   **Error Response (404 Not Found):** `"Announcement not found"`

### 16.3. Create Announcement (Auto Teams Mirror)
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Required Permission:** `ANNOUNCEMENT_CREATE`
*   **Request Body (`Feed` JSON):**
    ```json
    {
      "title": "System Outage Scheduled",
      "content": "Database maintenance tonight from 2 AM to 4 AM UTC.",
      "severity": "WARNING",
      "publishToInternal": true,
      "publishToTeams": true
    }
    ```
*   **Success Response (201 Created):** Returns the created announcement. If `publishToTeams` is enabled, triggers a call to the MS Teams Workflow Webhook, setting status to `SUCCESS` or `FAILED` without failing the DB transaction if the Webhook fails.

### 16.4. Delete Announcement
*   **HTTP Method:** `DELETE`
*   **Path:** `/{id}`
*   **Required Permission:** `ANNOUNCEMENT_DELETE`
*   **Success Response (200 OK):** `"Announcement deleted successfully"`

### 16.5. Manual Post App Message to Teams Channel
*   **HTTP Method:** `POST`
*   **Path:** `/teams-post`
*   **Required Permission:** `TEAMS_POST`
*   **Request Body (`TeamsPostRequest` JSON):**
    ```json
    {
      "title": "Manual Notification",
      "message": "This is a direct channel message from the admin daemon."
    }
    ```
*   **Success Response (200 OK):** `"Message posted to Teams successfully"`
*   **Error Responses:**
    *   `400 Bad Request`: Message content is missing.
    *   `429 Too Many Requests`: Teams Webhook rate limit exceeded.
    *   `500 Internal Server Error`: Teams Webhook call failed.

---

## 17. Meetings Module
**Base Path:** `/api/v1/meetings` (Requires HTTPOnly cookies)

### 17.1. Create Meeting
*   **HTTP Method:** `POST`
*   **Path:** `/`
*   **Required Permission:** `MEETING_CREATE`
*   **Request Body (`Meeting` JSON):**
    ```json
    {
      "title": "Sprint Planning",
      "description": "Q3 Sprint 1",
      "meetingLink": "https://teams.microsoft.com/...",
      "startTime": "2026-06-17T10:00:00",
      "durationMinutes": 45,
      "project": { "id": 1 },
      "task": { "id": 5 },
      "attendees": [
        { "id": 2 },
        { "id": 5 }
      ]
    }
    ```
*   **Success Response (201 Created):** Returns the created meeting.

### 17.2. Get All Meetings
*   **HTTP Method:** `GET`
*   **Path:** `/`
*   **Required Permission:** `MEETING_VIEW`
*   **Success Response (200 OK):** Returns a list of all scheduled meetings (ordered by start time).

### 17.3. Get Personal Meetings
*   **HTTP Method:** `GET`
*   **Path:** `/personal`
*   **Required Permission:** `MEETING_VIEW`
*   **Success Response (200 OK):** Returns a list of meetings where the authenticated user is either the creator or a listed attendee.

### 17.4. Update Meeting
*   **HTTP Method:** `PUT`
*   **Path:** `/{id}`
*   **Required Permission:** `MEETING_UPDATE`
*   **Request Body (`Meeting` JSON):** Same format as Create Meeting.
*   **Success Response (200 OK):** Returns the updated meeting.
*   **Error Response (403 Forbidden):** `"You are not authorized to update this meeting"` (if caller is not creator and not management).

### 17.5. Delete Meeting
*   **HTTP Method:** `DELETE`
*   **Path:** `/{id}`
*   **Required Permission:** `MEETING_DELETE`
*   **Success Response (200 OK):** `"Meeting deleted successfully"`
*   **Error Response (403 Forbidden):** `"You are not authorized to delete this meeting"` (if caller is not creator and not management).

### 17.6. Add Attendee
*   **HTTP Method:** `POST`
*   **Path:** `/{id}/attendees/{employeeId}`
*   **Required Permission:** `MEETING_UPDATE`
*   **Success Response (200 OK):** Returns the updated meeting with the new attendee added.
*   **Error Response (403 Forbidden):** `"You are not authorized to modify attendees for this meeting"`

### 17.7. Remove Attendee
*   **HTTP Method:** `DELETE`
*   **Path:** `/{id}/attendees/{employeeId}`
*   **Required Permission:** `MEETING_UPDATE`
*   **Success Response (200 OK):** Returns the updated meeting with the attendee removed.
*   **Error Response (403 Forbidden):** `"You are not authorized to modify attendees for this meeting"`

### 17.8. Get Meeting Attachments
*   **HTTP Method:** `GET`
*   **Path:** `/{id}/attachments`
*   **Required Permission:** `ATTACHMENT_VIEW`
*   **Success Response (200 OK):** Returns a list of all `Attachment` objects associated with this meeting.
*   **Error Response (404 Not Found):** `"Meeting not found"`

---

## 18. Membership Behavior & Access Rules Matrix

The following tables describe the membership behavior and cross-entity authorization checks (managed dynamically in the service layer) for Teams, Projects, Dynamic Role Assignment, and Tasks:

### 18.1. Teams Module Behavior
| Action | Admin | Team Lead | Sub Lead | Employee | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Create Team** | Allowed globally | Allowed globally | Allowed globally | Blocked (403) | |
| **View Teams List** | All Teams | Teams they lead/sublead/belong to | Teams they lead/sublead/belong to | Teams they belong to | |
| **View Team By ID** | Any Team | Lead/SubLead/Member | Lead/SubLead/Member | Member | |
| **Update Team** | Any Team | Teams they lead | Teams they sublead | Blocked (403) | |
| **Delete Team** | Any Team | Teams they lead | Teams they sublead | Blocked (403) | |
| **Unassign SubLead** | Any Team | Teams they lead | Blocked (403) | Blocked (403) | |
| **View Team Members** | Any Team | Teams they lead/sublead/member of | Teams they lead/sublead/member of | Teams they belong to | |
| **Add Team Members** | Any Team | Teams they lead | Teams they sublead | Blocked (403) | |
| **Remove Team Members** | Any Team | Teams they lead | Teams they sublead | Blocked (403) | |
| **View Another Employee's Teams** | Allowed | Blocked (403) | Blocked (403) | Blocked (403) | |
| **View Own Teams** | Allowed | Allowed | Allowed | Allowed | |

### 18.2. Projects & Project Management Behavior
| Action | Admin | Team Lead | Sub Lead | Employee | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Create Project** | Allowed globally | Allowed globally | Allowed globally | Blocked (403) | |
| **View Projects List** | All Projects | Projects they belong to + projects of their team members | Projects they belong to + projects of their team members | Projects they belong to | |
| **View Project By ID** | Any Project | Must be project member or team member must be member of project | Must be project member or team member must be member of project | Must be Project Member | |
| **View Project Members** | Any Project | Must be project member or team member must be member of project | Must be project member or team member must be member of project | Must be Project Member | |
| **View Own Projects** | Allowed | Allowed | Allowed | Allowed | |
| **View Another Employee's Projects** | Allowed | Allowed if target employee is team member of team they lead | Allowed if target employee is team member of team they sublead | Blocked (403) | |
| **Update Project** | Any Project | Projects they belong to + projects of their team members | Projects they belong to + projects of their team members | Blocked (403) | Requires Lead/SubLead + Membership or Managed Team Projects |
| **Delete Project** | Any Project | Projects they belong to + projects of their team members | Projects they belong to + projects of their team members | Blocked (403) | Requires Lead/SubLead + Membership or Managed Team Projects |
| **Add Project Members** | Any Project | Projects they belong to + projects of their team members | Projects they belong to + projects of their team members | Blocked (403) | Requires Lead/SubLead + Membership or Managed Team Projects |
| **Remove Project Members** | Any Project | Projects they belong to + projects of their team members | Projects they belong to + projects of their team members | Blocked (403) | Requires Lead/SubLead + Membership or Managed Team Projects |

### 18.3. Dynamic Role Assignment
*   **Employee becomes Team Lead of an ACTIVE team**: Gets `TEAM_LEAD` role.
*   **Employee becomes Sub Lead of an ACTIVE team**: Gets `SUB_LEAD` role.
*   **Employee no longer leads any ACTIVE team**: `TEAM_LEAD` role removed.
*   **Employee no longer subleads any ACTIVE team**: `SUB_LEAD` role removed.
*   **Employee is only a regular team member**: `EMPLOYEE` role only.

### 18.4. Tasks Module Behavior
| Action | Admin | Team Lead | Sub Lead | Employee | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Create Task** | Any Project | Visible Projects | Visible Projects | Blocked (403) | Requires project access |
| **View All Tasks** | All Tasks | Own Tasks + Managed Team Project Tasks | Own Tasks + Managed Team Project Tasks | Own Assigned Tasks | |
| **View Task By ID** | Any Task | Visible Task | Visible Task | Only Assigned Task | |
| **Update Task** | Any Task | Visible Task | Visible Task | Blocked (403) | |
| **Delete Task** | Any Task | Visible Task | Visible Task | Blocked (403) | |
| **Unassign Task** | Any Task | Visible Task | Visible Task | Blocked (403) | |
| **View Tasks By Employee ID** | Any Employee | Managed Employees + Self | Managed Employees + Self | Self Only | |
| **View Tasks By Project ID** | Any Project | Visible Projects | Visible Projects | Only own tasks within project | |
| **View Backlog Tasks** | All Backlog Tasks | All Backlog Tasks | All Backlog Tasks | All Backlog Tasks | Currently unsecured in code |

### 18.5. Task Comments Module Behavior
| Action | Admin | Team Lead | Sub Lead | Employee | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Add Comment** | Allowed | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Allowed on tasks assigned to self | Requires task visibility |
| **Get Comments** | Allowed (all tasks) | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Allowed on tasks assigned to self | Requires task visibility |
| **Delete Comment** | Allowed | Allowed only if own comment | Allowed only if own comment | Allowed only if own comment | Only Admin or comment Author can delete |

### 18.6. Task Tags & Tag Mapping Module Behavior
| Action | Admin | Team Lead | Sub Lead | Employee | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Create Task Tag** | Allowed globally | Allowed globally | Allowed globally | Blocked (403) | Requires manager role |
| **Get All Task Tags** | Allowed globally | Allowed globally | Allowed globally | Allowed globally | |
| **Get Task Tag By ID** | Allowed globally | Allowed globally | Allowed globally | Allowed globally | |
| **Delete Task Tag** | Allowed globally | Allowed globally | Allowed globally | Blocked (403) | Requires manager role |
| **Add Tag to Task** | Allowed on visible tasks | Allowed on visible tasks | Allowed on visible tasks | Blocked (403) | Requires manager role + task visibility |
| **Remove Tag from Task** | Allowed on visible tasks | Allowed on visible tasks | Allowed on visible tasks | Blocked (403) | Requires manager role + task visibility |
| **Get Tags for Task** | Allowed on visible tasks | Allowed on visible tasks | Allowed on visible tasks | Allowed on assigned tasks | Requires task visibility |

### 18.7. Task Attachments Module Behavior
| Action | Admin | Team Lead | Sub Lead | Employee | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Upload Attachment** | Allowed on any task | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Allowed on tasks assigned to self | Requires task visibility |
| **Download Attachment** | Allowed | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Allowed on tasks assigned to self | Requires task visibility |
| **Get Attachment Metadata** | Allowed | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Allowed on tasks assigned to self | Requires task visibility |
| **Delete Attachment** | Allowed | Allowed if own file or if uploader is a managed team member | Allowed if own file or if uploader is a managed team member | Allowed if own file | Restricted to Admin, Uploader, or Uploader's Team Lead/Sub Lead |

### 18.8. ETA Extension Requests Module Behavior
| Action | Admin | Team Lead | Sub Lead | Employee | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Create ETA Request** | Blocked (403) unless task assigned to self | Blocked (403) unless task assigned to self | Blocked (403) unless task assigned to self | Allowed on tasks assigned to self | Requires task assignee status |
| **View Request By ID** | Allowed | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Allowed on own requests | Requires task visibility |
| **View Task Requests List** | Allowed | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Allowed on tasks assigned to self | Requires task visibility |
| **Approve ETA Request** | Allowed globally | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Blocked (403) | Requires manager role + task visibility |
| **Reject ETA Request** | Allowed globally | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Blocked (403) | Requires manager role + task visibility |
| **Undo Request Decision** | Allowed globally | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Blocked (403) | Requires manager role + task visibility |

### 18.9. Task Transfer Requests Module Behavior
| Action | Admin | Team Lead | Sub Lead | Employee | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Create Transfer Request** | Blocked (403) unless task assigned to self | Blocked (403) unless task assigned to self | Blocked (403) unless task assigned to self | Allowed on tasks assigned to self | Target employee must belong to same project |
| **View Request By ID** | Allowed | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Allowed on own requests | Requires task visibility |
| **View Task Requests List** | Allowed | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Allowed on tasks assigned to self | Requires task visibility |
| **Approve Transfer Request** | Allowed globally | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Blocked (403) | Target employee must still be member of project |
| **Reject Transfer Request** | Allowed globally | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Blocked (403) | Requires manager role + task visibility |
| **Undo Request Decision** | Allowed globally | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Blocked (403) | Re-assigns task back to original requester |

### 18.10. Task Status History Module Behavior
| Action | Admin | Team Lead | Sub Lead | Employee | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Create Status History** | Automatic/System | Automatic/System | Automatic/System | Automatic/System | Triggered during task updates/transfers/ETA extension decisions |
| **View Task Status History** | Allowed | Allowed on tasks in visible projects | Allowed on tasks in visible projects | Allowed on tasks assigned to self | Requires task visibility |

### 18.11. Timesheet Module Behavior
| Action | Admin | Team Lead | Sub Lead | Employee | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Create Timesheet Entry** | Allowed globally | Allowed for self | Allowed for self | Allowed for self | Must belong to project; timesheet entries cannot overlap; blocked on completed or review tasks |
| **View Timesheet Entries** | Allowed globally | Allowed for self + managed team members | Allowed for self + managed team members | Allowed for self only | Filters by managed team members if no ID is specified |
| **Approve/Reject Entry** | Allowed globally | Allowed on managed team member entries | Allowed on managed team member entries | Blocked (403) | Cannot approve/reject own entry |
| **Patch Update Entry** | Allowed globally | Blocked (403) unless own entry | Blocked (403) unless own entry | Allowed for self only | Subject to task review rules, overlap checks, and project membership |
| **Delete Timesheet Entry** | Allowed globally | Blocked (403) unless own entry | Blocked (403) unless own entry | Allowed for self only | |

### 18.12. Attachment Module Behavior
| Action | Admin | Team Lead | Sub Lead | Employee | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Upload Attachment** | Allowed globally | Allowed globally | Allowed globally | Allowed globally | File size capped at 50 MB; stored in S3; linked to meeting via `meetingId` parameter |
| **View All Attachments** | Allowed globally | Allowed globally | Allowed globally | Allowed globally | Returns all records; no ownership filter |
| **View Attachment Metadata** | Allowed globally | Allowed globally | Allowed globally | Allowed globally | Returns single record by ID |
| **Download Attachment** | Allowed globally | Allowed globally | Allowed globally | Allowed globally | Streams directly from S3 with Content-Disposition header |
| **Delete Attachment** | Allowed globally | Allowed if own file or uploader is managed team member | Allowed if own file or uploader is managed team member | Allowed for own uploads only | Service enforces: Admin OR Uploader OR Uploader's Team Lead/Sub Lead |

### 18.13. Feed & Teams Module Behavior
| Action | Admin | Team Lead | Sub Lead | Employee | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **View Announcements** | Allowed globally | Allowed globally | Allowed globally | Allowed globally | Requires `ANNOUNCEMENT_VIEW` |
| **Create Announcement** | Allowed globally | Allowed globally | Allowed globally | Blocked (403) | Requires `ANNOUNCEMENT_CREATE` |
| **Delete Announcement** | Allowed globally | Allowed globally | Allowed globally | Blocked (403) | Requires `ANNOUNCEMENT_DELETE` |
| **Manual Teams Post** | Allowed globally | Allowed globally | Allowed globally | Allowed globally | Requires `TEAMS_POST`; posts to Teams via daemon credentials |

### 18.14. Meetings Module Behavior
| Action | Admin | Team Lead | Sub Lead | Employee | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Create Meeting** | Allowed globally | Allowed globally | Allowed globally | Allowed globally | Requires `MEETING_CREATE` |
| **View All Meetings** | Allowed globally | Allowed globally | Allowed globally | Allowed globally | Requires `MEETING_VIEW`; returns all scheduled meetings |
| **View Personal Meetings** | Allowed globally | Allowed globally | Allowed globally | Allowed globally | Requires `MEETING_VIEW`; returns meetings where user is attendee or creator |
| **Update Meeting** | Allowed globally | Allowed globally | Allowed globally | Allowed if creator | Requires `MEETING_UPDATE`; creator check matched by User ID |
| **Delete Meeting** | Allowed globally | Allowed globally | Allowed globally | Allowed if creator | Requires `MEETING_DELETE`; creator check matched by User ID |
| **Add/Remove Attendees** | Allowed globally | Allowed globally | Allowed globally | Allowed if creator | Requires `MEETING_UPDATE`; creator check matched by User ID |
| **Upload Attachment to Meeting** | Allowed globally | Allowed globally | Allowed globally | Allowed globally | Requires `ATTACHMENT_UPLOAD`; links uploaded file to meeting ID |
| **View Meeting Attachments** | Allowed globally | Allowed globally | Allowed globally | Allowed globally | Requires `ATTACHMENT_VIEW`; returns all attachments linked to meeting |
