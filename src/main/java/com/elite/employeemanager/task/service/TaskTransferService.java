package com.elite.employeemanager.task.service;

import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.project.repository.ProjectEmployeeRepository;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.entity.TaskTransfer;
import com.elite.employeemanager.task.repository.EtaExtensionRepository;
import com.elite.employeemanager.task.repository.TaskRepository;
import com.elite.employeemanager.task.repository.TaskTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskTransferService {

    private final TaskTransferRepository taskTransferRepository;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskStatusHistoryService taskStatusHistoryService;
    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final EtaExtensionRepository etaExtensionRepository;

    private User getCurrentUser(){
        Object principal = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if(principal instanceof User user) {
            return user;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }

    public TaskTransfer getTaskTransferById(Long id){
        return taskTransferRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task Transfer Request Not Found"));
    }

    @Transactional
    public TaskTransfer createTaskTransferRequest(TaskTransfer taskTransfer){

        if (taskTransfer.getTask() == null || taskTransfer.getTask().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task Id is required");
        }

        Task task = taskRepository.findById(taskTransfer.getTask().getId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task Not Found"));

        User user = getCurrentUser();
        Employee employee = employeeRepository.findByWorkEmail(user.getEmail())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));

        if (taskTransfer.getTargetEmployee() == null || taskTransfer.getTargetEmployee().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target Employee Id is required");
        }
        Employee targetEmployee = employeeRepository.findById(taskTransfer.getTargetEmployee().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target Employee Not Found"));
        if (Boolean.TRUE.equals(targetEmployee.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target Employee is inactive");
        }
        projectEmployeeRepository.findByProjectAndEmployee(task.getProject(), targetEmployee)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target employee does not belong to the project"));

        if (taskTransferRepository.existsByTaskAndStatus(task,"PENDING")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Task already has pending Transfer Request");
        }
        if ("COMPLETED".equalsIgnoreCase(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot request Task Transfer for a completed task");
        }
        if (Boolean.TRUE.equals(task.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot request Task Transfer for a deleted task");
        }
        if (task.getAssignedTo()==null || !task.getAssignedTo().getId().equals(employee.getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Employee not assigned to this task");
        }
        if (taskTransfer.getReason()==null||taskTransfer.getReason().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Reason for Task Transfer required");
        }
        if (targetEmployee.getId().equals(employee.getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Employee already assigned to this task");
        }


        taskTransfer.setCreatedAt(LocalDateTime.now());
        taskTransfer.setTask(task);
        taskTransfer.setRequestedBy(employee);
        taskTransfer.setTargetEmployee(targetEmployee);
        taskTransfer.setStatus("PENDING");

        return taskTransferRepository.save(taskTransfer);
    }

    @Transactional
    public TaskTransfer approveTaskTransferRequest(Long requestId){
        TaskTransfer request = getTaskTransferById(requestId);
        Task task = request.getTask();

        if (Boolean.TRUE.equals(request.getTargetEmployee().getIsDeleted())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Target Employee is inactive");
        }
        if (Boolean.TRUE.equals(task.getIsDeleted())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Task has been deleted");
        }
        if ("COMPLETED".equalsIgnoreCase(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot approve transfer for a completed task");
        }
        if (task.getAssignedTo() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Task is no longer assigned");
        }
        if (!task.getAssignedTo().getId()
                .equals(request.getRequestedBy().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Task is no longer assigned to the requesting employee");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request already processed");
        }

        projectEmployeeRepository.findByProjectAndEmployee(task.getProject(), request.getTargetEmployee())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target employee does not belong to the project"));

        request.setStatus("APPROVED");
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(getCurrentUser());

        task.setAssignedTo(request.getTargetEmployee());
        taskRepository.save(task);
        etaExtensionRepository.deleteByTaskAndStatus(task, "PENDING");

        taskStatusHistoryService.createTaskStatusHistory(
                task,
                task.getStatus(),
                task.getStatus(),
                getCurrentUser(),
                "Task Transferred from "
                        + request.getRequestedBy().getName()
                        + " to "
                        + request.getTargetEmployee().getName()
        );

        return taskTransferRepository.save(request);
    }

    @Transactional
    public TaskTransfer rejectTaskTransferRequest(Long requestId, String reason){
        TaskTransfer request = getTaskTransferById(requestId);

        if (!"PENDING".equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request already processed");
        }
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejection reason is required");
        }

        request.setStatus("REJECTED");
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(getCurrentUser());
        request.setRejectionReason(reason);

        taskStatusHistoryService.createTaskStatusHistory(
                request.getTask(),
                request.getTask().getStatus(),
                request.getTask().getStatus(),
                getCurrentUser(),
                "Task transfer request to " + request.getTargetEmployee().getName() + " rejected: " + reason
        );

        return taskTransferRepository.save(request);
    }

    @Transactional
    public TaskTransfer undoDecision(Long requestId){
        TaskTransfer request = getTaskTransferById(requestId);
        Task task = request.getTask();
        if (Boolean.TRUE.equals(task.getIsDeleted())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Task has been deleted");
        }
        if ("PENDING".equals(request.getStatus())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Request has not been processed yet");
        }

        if ("APPROVED".equals(request.getStatus())){
            if (Boolean.TRUE.equals(request.getRequestedBy().getIsDeleted())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Requested Employee has been deleted");
            }
            if (task.getAssignedTo() == null ||
                    !task.getAssignedTo().getId().equals(request.getTargetEmployee().getId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Task is no longer assigned to the target employee");
            }
            projectEmployeeRepository.findByProjectAndEmployee(task.getProject(), request.getRequestedBy())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested employee does not belong to the project"));
            task.setAssignedTo(request.getRequestedBy());
            taskRepository.save(task);

            taskStatusHistoryService.createTaskStatusHistory(
                    task,
                    task.getStatus(),
                    task.getStatus(),
                    getCurrentUser(),
                    "Undid task transfer. Reassigned task from "
                            + request.getTargetEmployee().getName()
                            + " back to "
                            + request.getRequestedBy().getName()
            );
        } else if ("REJECTED".equals(request.getStatus())) {
            taskStatusHistoryService.createTaskStatusHistory(
                    task,
                    task.getStatus(),
                    task.getStatus(),
                    getCurrentUser(),
                    "Undid task transfer rejection (original rejection reason: " + request.getRejectionReason() + ")"
            );
        }

        request.setStatus("PENDING");
        request.setReviewedAt(null);
        request.setReviewedBy(null);
        request.setRejectionReason(null);

        return taskTransferRepository.save(request);
    }

    public List<TaskTransfer> getTaskTransferByTaskId(Long taskId){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task Not Found"));

        return taskTransferRepository.findByTask(task);
    }

}
