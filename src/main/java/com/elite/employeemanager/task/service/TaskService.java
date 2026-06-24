package com.elite.employeemanager.task.service;

import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.project.entity.Project;
import com.elite.employeemanager.project.entity.ProjectEmployee;
import com.elite.employeemanager.project.repository.ProjectEmployeeRepository;
import com.elite.employeemanager.project.repository.ProjectRepository;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.repository.EtaExtensionRepository;
import com.elite.employeemanager.task.repository.TaskCommentRepository;
import com.elite.employeemanager.task.repository.TaskRepository;
import com.elite.employeemanager.task.repository.TaskTagMappingRepository;
import com.elite.employeemanager.task.utility.TaskUtility;
import com.elite.employeemanager.team.entity.Team;
import com.elite.employeemanager.team.entity.TeamEmployee;
import com.elite.employeemanager.team.repository.TeamEmployeeRepository;
import com.elite.employeemanager.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import com.elite.employeemanager.timesheet.repository.TimesheetEntryRepository;
import com.elite.employeemanager.timesheet.entity.TimesheetEntry;
import com.elite.employeemanager.task.dto.TaskReviewSubmitRequest;
import com.elite.employeemanager.task.dto.TaskReviewRequest;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;
    private final TeamEmployeeRepository teamEmployeeRepository;
    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final TaskStatusHistoryService taskStatusHistoryService;
    private final EtaExtensionRepository etaExtensionRepository;
    private final SecurityUtils securityUtils;
    private final TaskUtility taskUtility;
    private final TimesheetEntryRepository timesheetEntryRepository;

    public Task createTask(Task task){

        if (task.getEtaHours() == null) {
            task.setEtaHours(new java.math.BigDecimal("0.00"));
        }
        if (task.getTaskNumber()==null || task.getTaskNumber().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Task Number is required");
        }
        if (taskRepository.existsByTaskNumber(task.getTaskNumber())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Task Number already exists");
        }
        if (task.getTitle()==null || task.getTitle().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Task Title is required");
        }
        if (task.getProject()==null || task.getProject().getId()==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Project is required");
        }
        Project project = projectRepository.findById(task.getProject().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        task.setProject(project);

        taskUtility.validateProjectManagementAccess(task);

        if (task.getTaskType()==null||task.getTaskType().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Task Type is required");
        }
        if (task.getStatus()==null){
            task.setStatus("OPEN");
        }
        if (task.getPriority()==null||task.getPriority().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Task Priority is required");
        }
        if (task.getEtaHours()==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Task Eta Hours is required");
        }
        if (task.getEtaDate()==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Task Eta Date is required");
        }
        task.setOriginalEtaDate(task.getEtaDate());

        if (task.getAssignedTo()!=null && task.getAssignedTo().getId()!=null){
            Employee employee = employeeRepository.findById(task.getAssignedTo().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
            projectEmployeeRepository.findByProjectAndEmployee(project,employee)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee does not belong to this project"));
            task.setAssignedTo(employee);
        }

        if ("BUG".equalsIgnoreCase(task.getTaskType())){
            if (task.getBugNumber()==null || task.getBugNumber().isBlank()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Bug number is required if task type is Bug");
            }
        }
        else {
            task.setBugNumber(null);
        }

        return taskRepository.save(task);
    }

    public List<Task> getAllTasks(){
        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN")){

            List<Task> assignedTasks = taskRepository.findByAssignedTo(currentEmployee);

            if (!currentEmployee.getRoles().contains("TEAM_LEAD") && !currentEmployee.getRoles().contains("SUB_LEAD")){
                return assignedTasks;
            }

            List<Team> leadTeams = teamRepository.findByLead(currentEmployee);
            List<Team> subLeadTeams = teamRepository.findBySubLead(currentEmployee);

            List<Task> leadTeamMemberProjectTasks = List.of();
            if (!leadTeams.isEmpty()) {

                List<Employee> leadTeamMembers = teamEmployeeRepository.findByTeamIn(leadTeams)
                        .stream()
                        .map(TeamEmployee::getEmployee)
                        .distinct()
                        .toList();

                if (!leadTeamMembers.isEmpty()) {

                    List<Project> leadTeamMemberProjects = projectEmployeeRepository.findByEmployeeIn(leadTeamMembers)
                            .stream()
                            .map(ProjectEmployee::getProject)
                            .distinct()
                            .toList();

                    if (!leadTeamMemberProjects.isEmpty()) {
                        leadTeamMemberProjectTasks = taskRepository.findByProjectIn(leadTeamMemberProjects);
                    }
                }
            }

            List<Task> subLeadTeamMemberProjectTasks = List.of();
            if (!subLeadTeams.isEmpty()) {

                List<Employee> subLeadTeamMembers = teamEmployeeRepository.findByTeamIn(subLeadTeams)
                        .stream()
                        .map(TeamEmployee::getEmployee)
                        .distinct()
                        .toList();

                if (!subLeadTeamMembers.isEmpty()) {

                    List<Project> subLeadTeamMemberProjects = projectEmployeeRepository.findByEmployeeIn(subLeadTeamMembers)
                            .stream()
                            .map(ProjectEmployee::getProject)
                            .distinct()
                            .toList();

                    if (!subLeadTeamMemberProjects.isEmpty()) {
                        subLeadTeamMemberProjectTasks = taskRepository.findByProjectIn(subLeadTeamMemberProjects);
                    }
                }
            }

            List<Task> currentLeadProjectTasks = List.of();

            List<Project> currentLeadProjects = projectEmployeeRepository.findByEmployee(currentEmployee)
                    .stream()
                    .map(ProjectEmployee::getProject)
                    .distinct()
                    .toList();

            if (!currentLeadProjects.isEmpty()) {
                currentLeadProjectTasks = taskRepository.findByProjectIn(currentLeadProjects);
            }

            return Stream.of(leadTeamMemberProjectTasks,subLeadTeamMemberProjectTasks,currentLeadProjectTasks,assignedTasks)
                    .flatMap(List::stream)
                    .distinct()
                    .toList();
        }
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id){

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found with id : " + id
                ));

        Employee currentEmployee = securityUtils.getCurrentEmployee();

        if (currentEmployee.getRoles().contains("ADMIN")) {
            return task;
        }

        if (task.getAssignedTo() != null &&
                task.getAssignedTo().getId().equals(currentEmployee.getId())) {
            return task;
        }

        if (currentEmployee.getRoles().contains("TEAM_LEAD")
                || currentEmployee.getRoles().contains("SUB_LEAD")) {

            List<Team> leadTeams = teamRepository.findByLead(currentEmployee);
            List<Team> subLeadTeams = teamRepository.findBySubLead(currentEmployee);

            List<Employee> leadTeamMembers = teamEmployeeRepository.findByTeamIn(leadTeams)
                    .stream()
                    .map(TeamEmployee::getEmployee)
                    .distinct()
                    .toList();

            List<Employee> subLeadTeamMembers = teamEmployeeRepository.findByTeamIn(subLeadTeams)
                    .stream()
                    .map(TeamEmployee::getEmployee)
                    .distinct()
                    .toList();

            List<Project> visibleProjects = Stream.of(
                            projectEmployeeRepository.findByEmployeeIn(leadTeamMembers)
                                    .stream()
                                    .map(ProjectEmployee::getProject)
                                    .toList(),

                            projectEmployeeRepository.findByEmployeeIn(subLeadTeamMembers)
                                    .stream()
                                    .map(ProjectEmployee::getProject)
                                    .toList(),

                            projectEmployeeRepository.findByEmployee(currentEmployee)
                                    .stream()
                                    .map(ProjectEmployee::getProject)
                                    .toList()
                    )
                    .flatMap(List::stream)
                    .distinct()
                    .toList();

            if (task.getProject() != null &&
                    visibleProjects.contains(task.getProject())) {
                return task;
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not allowed to view this task");

    }

    @Transactional
    public Task updateTaskById(Long id, Task task){

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN") && !currentEmployee.getRoles().contains("TEAM_LEAD") && !currentEmployee.getRoles().contains("SUB_LEAD")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not allowed to modify this task");
        }

        Task existingTask = getTaskById(id);

        if (task.getTaskNumber() != null && !task.getTaskNumber().isBlank()) {
            if (!existingTask.getTaskNumber().equals(task.getTaskNumber()) && taskRepository.existsByTaskNumber(task.getTaskNumber())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task Number already exists");
            }
            existingTask.setTaskNumber(task.getTaskNumber());
        }

        if (task.getTitle() != null && !task.getTitle().isBlank()) {
            existingTask.setTitle(task.getTitle());
        }

        if (task.getDescription() != null) {
            existingTask.setDescription(task.getDescription());
        }

        if (task.getTaskType() != null && !task.getTaskType().isBlank()) {
            existingTask.setTaskType(task.getTaskType());
        }

        if (task.getPriority() != null && !task.getPriority().isBlank()) {
            existingTask.setPriority(task.getPriority());
        }

        String oldStatus =existingTask.getStatus();
        boolean statusChanged = false;
        if (task.getStatus() != null && !task.getStatus().isBlank() && !task.getStatus().equals(existingTask.getStatus())) {
            existingTask.setStatus(task.getStatus());
            statusChanged=true;
        }

        if (task.getEtaHours() != null) {
            existingTask.setEtaHours(task.getEtaHours());
        }

        if (task.getEtaDate() != null) {
            existingTask.setEtaDate(task.getEtaDate());
        }

        if (task.getEpic() != null) {
            existingTask.setEpic(task.getEpic());
        }

        if ((task.getProject()!=null && task.getProject().getId()!=null) &&
                (task.getAssignedTo() != null && task.getAssignedTo().getId() != null)){

            Project project = projectRepository.findById(task.getProject().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Project not found"));
            Employee employee = employeeRepository
                    .findById(task.getAssignedTo().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Employee not found"));
            projectEmployeeRepository
                    .findByProjectAndEmployee(
                            project,
                            employee)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "New Employee does not belong to the new project"));
            existingTask.setProject(project);
            existingTask.setAssignedTo(employee);
        }
        else {
            if (task.getProject()!=null && task.getProject().getId()!=null){
                Project project = projectRepository.findById(task.getProject().getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Project not found"));
                if (existingTask.getAssignedTo() != null) {
                    projectEmployeeRepository
                            .findByProjectAndEmployee(
                                    project,
                                    existingTask.getAssignedTo())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Current assignee does not belong to the new project"));
                }
                existingTask.setProject(project);
            }

            if (task.getAssignedTo() != null && task.getAssignedTo().getId() != null) {
                Employee employee = employeeRepository
                        .findById(task.getAssignedTo().getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Employee not found"));
                if (existingTask.getProject()!=null){
                    projectEmployeeRepository
                            .findByProjectAndEmployee(existingTask.getProject(), employee)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Employee does not belong to this project"));
                }
                existingTask.setAssignedTo(employee);
            }
        }


        if ("BUG".equalsIgnoreCase(existingTask.getTaskType())) {

            if (task.getBugNumber()!=null){
                existingTask.setBugNumber(task.getBugNumber());
            }

            if (existingTask.getBugNumber() == null || existingTask.getBugNumber().isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Bug number is required if task type is bug");
            }
        }
        else {
            existingTask.setBugNumber(null);
        }

        Task savedTask = taskRepository.save(existingTask);

        if (statusChanged) {
            taskStatusHistoryService.createTaskStatusHistory(
                    savedTask,
                    oldStatus,
                    savedTask.getStatus(),
                    securityUtils.getCurrentUser(),
                    null
            );
        }

        return savedTask;
    }

    @Transactional
    public void unassignTaskById(Long id){

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN") && !currentEmployee.getRoles().contains("TEAM_LEAD") && !currentEmployee.getRoles().contains("SUB_LEAD")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not allowed to modify this task");
        }

        Task task = getTaskById(id);
        task.setAssignedTo(null);
        etaExtensionRepository.deleteByTaskAndStatus(task,"PENDING");
        taskRepository.save(task);
    }

    @Transactional
    public void deleteTaskById(Long id, String reason){

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN") && !currentEmployee.getRoles().contains("TEAM_LEAD") && !currentEmployee.getRoles().contains("SUB_LEAD")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not allowed to modify this task");
        }

        Task task = getTaskById(id);

        task.setIsDeleted(true);
        task.setDeletedAt(LocalDateTime.now());
        task.setDeleteReason(reason);
        task.setDeletedBy(securityUtils.getCurrentUser().getId());
        taskRepository.save(task);
    }

    public List<Task> getTasksByEmployeeId(Long employeeId){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));

        Employee currentEmployee = securityUtils.getCurrentEmployee();

        if (currentEmployee.getRoles().contains("ADMIN")) {
            return taskRepository.findByAssignedTo(employee);
        }

        if (currentEmployee.getId().equals(employeeId)) {
            return taskRepository.findByAssignedTo(employee);
        }

        if (currentEmployee.getRoles().contains("TEAM_LEAD") || currentEmployee.getRoles().contains("SUB_LEAD")) {

            List<Team> leadTeams = teamRepository.findByLead(currentEmployee);
            List<Team> subLeadTeams = teamRepository.findBySubLead(currentEmployee);

            boolean isManagedEmployee =
                    teamEmployeeRepository.findByTeamIn(leadTeams).stream()
                            .map(TeamEmployee::getEmployee)
                            .anyMatch(e -> e.getId().equals(employeeId))
                            ||
                            teamEmployeeRepository.findByTeamIn(subLeadTeams).stream()
                                    .map(TeamEmployee::getEmployee)
                                    .anyMatch(e -> e.getId().equals(employeeId));

            if (isManagedEmployee) {
                return taskRepository.findByAssignedTo(employee);
            }
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Current User is not allowed to view this employee's tasks");
    }

    public List<Task> getTasksByProjectId(Long projectId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project Not Found"));

        Employee currentEmployee = securityUtils.getCurrentEmployee();

        if (currentEmployee.getRoles().contains("ADMIN")) {
            return taskRepository.findByProject(project);
        }

        if (!currentEmployee.getRoles().contains("TEAM_LEAD")
                && !currentEmployee.getRoles().contains("SUB_LEAD")) {

            return taskRepository.findByProjectAndAssignedTo(
                    project,
                    currentEmployee
            );
        }

        if (currentEmployee.getRoles().contains("TEAM_LEAD") || currentEmployee.getRoles().contains("SUB_LEAD")) {

            List<Team> leadTeams = teamRepository.findByLead(currentEmployee);
            List<Team> subLeadTeams = teamRepository.findBySubLead(currentEmployee);

            List<Employee> managedEmployees = Stream.of(
                            teamEmployeeRepository.findByTeamIn(leadTeams),
                            teamEmployeeRepository.findByTeamIn(subLeadTeams)
                    )
                    .flatMap(List::stream)
                    .map(TeamEmployee::getEmployee)
                    .distinct()
                    .toList();

            boolean teamMemberInProject =
                    projectEmployeeRepository.findByEmployeeIn(managedEmployees)
                            .stream()
                            .anyMatch(pe -> pe.getProject().getId().equals(projectId));

            boolean currentLeadInProject =
                    projectEmployeeRepository.findByEmployee(currentEmployee)
                            .stream()
                            .anyMatch(pe -> pe.getProject().getId().equals(projectId));

            if (teamMemberInProject || currentLeadInProject) {
                return taskRepository.findByProject(project);
            }
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Current User is not allowed to view tasks for this project");
    }

    public List<Task> getBacklogTasks(){
        return taskRepository.findByAssignedToIsNull();
    }

    @Transactional
    public Task submitForReview(Long id, TaskReviewSubmitRequest request) {
        Task task = getTaskById(id);
        Employee currentEmployee = securityUtils.getCurrentEmployee();

        if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(currentEmployee.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only submit tasks assigned to you");
        }

        List<TimesheetEntry> logs = timesheetEntryRepository.findByTask(task);
        BigDecimal totalHoursLogged = logs.stream()
                .map(TimesheetEntry::getHoursSpent)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        boolean isHoursBreached = totalHoursLogged.compareTo(task.getEtaHours()) > 0;
        boolean isDateBreached = LocalDate.now().isAfter(task.getEtaDate());

        if (isHoursBreached || isDateBreached) {
            if (request.getJustification() == null || request.getJustification().isBlank()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Task has exceeded ETA. A justification is required to submit for review."
                );
            }
            task.setJustification(request.getJustification());
        }

        String oldStatus = task.getStatus();
        task.setStatus("PENDING_REVIEW");

        taskStatusHistoryService.createTaskStatusHistory(
                task,
                oldStatus,
                "PENDING_REVIEW",
                securityUtils.getCurrentUser(),
                "Submitted for completion review"
        );

        return taskRepository.save(task);
    }

    @Transactional
    public Task reviewTask(Long id, TaskReviewRequest request) {
        Employee currentEmployee = securityUtils.getCurrentEmployee();
        boolean isAdmin = currentEmployee.getRoles().contains("ADMIN");
        boolean isLead = currentEmployee.getRoles().contains("TEAM_LEAD") || currentEmployee.getRoles().contains("SUB_LEAD");

        if (!isAdmin && !isLead) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not allowed to review this task");
        }

        Task task = getTaskById(id);

        if (!isAdmin) {
            List<Team> managedTeams = new java.util.ArrayList<>();
            if (currentEmployee.getRoles().contains("TEAM_LEAD")) {
                managedTeams.addAll(teamRepository.findByLead(currentEmployee));
            }
            if (currentEmployee.getRoles().contains("SUB_LEAD")) {
                managedTeams.addAll(teamRepository.findBySubLead(currentEmployee));
            }

            boolean isMemberOfManagedTeam = false;
            if (task.getAssignedTo() != null) {
                for (Team team : managedTeams) {
                    if (teamEmployeeRepository.findByTeamAndEmployee(team, task.getAssignedTo()).isPresent()) {
                        isMemberOfManagedTeam = true;
                        break;
                    }
                }
            }

            if (!isMemberOfManagedTeam) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to review tasks for this employee");
            }
        }

        String targetStatus = request.getStatus().toUpperCase();
        if (!"APPROVED".equals(targetStatus) && !"REJECTED".equals(targetStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review status must be either APPROVED or REJECTED");
        }

        String oldStatus = task.getStatus();
        String newStatus;

        if ("APPROVED".equals(targetStatus)) {
            newStatus = "COMPLETED";
            task.setStatus(newStatus);
            task.setCompletionReviewStatus("APPROVED");
        } else {
            newStatus = "IN_PROGRESS";
            task.setStatus(newStatus);
            task.setCompletionReviewStatus("REJECTED");
        }

        task.setReviewComment(request.getComment());

        taskStatusHistoryService.createTaskStatusHistory(
                task, 
                oldStatus, 
                newStatus, 
                securityUtils.getCurrentUser(), 
                "Task completion review: " + targetStatus + ". Comment: " + request.getComment()
        );

        return taskRepository.save(task);
    }

    public String determineStatusAfterUndo(Task task, String previousStatus) {
        if ("OPEN".equals(previousStatus) || "IN_PROGRESS".equals(previousStatus) || "OVER_ETA".equals(previousStatus) || "ETA_EXTENDED".equals(previousStatus)) {
            List<TimesheetEntry> logs = timesheetEntryRepository.findByTask(task);
            BigDecimal totalHoursLogged = logs.stream()
                    .map(TimesheetEntry::getHoursSpent)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            boolean isHoursBreached = totalHoursLogged.compareTo(task.getEtaHours()) > 0;
            boolean isDateBreached = LocalDate.now().isAfter(task.getEtaDate());

            if (isHoursBreached || isDateBreached) {
                return "OVER_ETA";
            }

            if ("OVER_ETA".equals(previousStatus)) {
                return "IN_PROGRESS";
            }

            if ("OPEN".equals(previousStatus)) {
                if (totalHoursLogged.compareTo(BigDecimal.ZERO) > 0) {
                    return "IN_PROGRESS";
                }
            }
        }
        return previousStatus;
    }

}
