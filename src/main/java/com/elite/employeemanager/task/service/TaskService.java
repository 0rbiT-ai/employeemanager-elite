package com.elite.employeemanager.task.service;

import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.project.entity.Project;
import com.elite.employeemanager.project.repository.ProjectEmployeeRepository;
import com.elite.employeemanager.project.repository.ProjectRepository;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.repository.TaskCommentRepository;
import com.elite.employeemanager.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final TaskCommentRepository taskCommentRepository;

    private User getCurrentUser(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal instanceof User) {
            return  ((User) principal);
        }
        return null;
    }

    public Task createTask(Task task){
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
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id){
        return taskRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task not found with id : "+id));
    }

    public Task updateTaskById(Long id, Task task){

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

        if (task.getStatus() != null && !task.getStatus().isBlank()) {
            existingTask.setStatus(task.getStatus());
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

        return taskRepository.save(existingTask);
    }

    public void unassignTaskById(Long id){
        Task task = getTaskById(id);
        task.setAssignedTo(null);
        taskRepository.save(task);
    }

    @Transactional
    public void deleteTaskById(Long id, String reason){
        Task task = getTaskById(id);

        taskCommentRepository.deleteByTask(task);

        task.setIsDeleted(true);
        task.setDeletedAt(LocalDateTime.now());
        task.setDeleteReason(reason);
        if(getCurrentUser()!=null){
            task.setDeletedBy(getCurrentUser().getId());
        }
        taskRepository.save(task);
    }

    public List<Task> getTasksByEmployeeId(Long employeeId){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));
        return taskRepository.findByAssignedTo(employee);
    }

    public List<Task> getTasksByProjectId(Long projectId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project Not Found"));
        return taskRepository.findByProject(project);
    }

    public List<Task> getBacklogTasks(){
        return taskRepository.findByAssignedToIsNull();
    }

}
