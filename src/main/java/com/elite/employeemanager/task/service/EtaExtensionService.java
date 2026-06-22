package com.elite.employeemanager.task.service;

import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.task.entity.EtaExtension;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.repository.EtaExtensionRepository;
import com.elite.employeemanager.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EtaExtensionService {

    private final EtaExtensionRepository etaExtensionRepository;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskStatusHistoryService taskStatusHistoryService;
    private final SecurityUtils securityUtils;
    private final TaskService taskService;

    public EtaExtension getEtaExtensionById(Long id){
        EtaExtension request = etaExtensionRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"ETA Request Not Found"));
        taskService.getTaskById(request.getTask().getId()); // task visibility check
        return request;
    }

    @Transactional
    public EtaExtension createEtaExtensionRequest(EtaExtension etaExtension){

        if (etaExtension.getTask() == null || etaExtension.getTask().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task Id is required");
        }

        Task task = taskService.getTaskById(etaExtension.getTask().getId());

        Employee employee = securityUtils.getCurrentEmployee();

        if (task.getAssignedTo() != null && !task.getAssignedTo().getId().equals(employee.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Task not assigned to the current user");
        }

        if (etaExtensionRepository.existsByTaskAndStatus(task,"PENDING")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Task already has pending ETA Request");
        }
        if ("COMPLETED".equalsIgnoreCase(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot request ETA extension for a completed task");
        }
        if (Boolean.TRUE.equals(task.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot request ETA extension for a deleted task");
        }
        if (task.getAssignedTo()==null || !task.getAssignedTo().getId().equals(employee.getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Employee not assigned to this task");
        }
        if (etaExtension.getReason()==null||etaExtension.getReason().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Reason for Eta Extension required");
        }
        if (etaExtension.getNewEtaDate()==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"New ETA Date required");
        }
        if (etaExtension.getNewEtaDate().equals(task.getEtaDate())||etaExtension.getNewEtaDate().isBefore(task.getEtaDate())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"New ETA Date cannot be on or before Old Eta Date");
        }

        etaExtension.setCreatedAt(LocalDateTime.now());
        etaExtension.setTask(task);
        etaExtension.setRequestedBy(employee);
        etaExtension.setOldEtaDate(task.getEtaDate());
        etaExtension.setStatus("PENDING");

        return etaExtensionRepository.save(etaExtension);
    }

    @Transactional
    public EtaExtension approveEtaExtensionRequest(Long requestId){

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN") && !currentEmployee.getRoles().contains("TEAM_LEAD") && !currentEmployee.getRoles().contains("SUB_LEAD")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not allowed to modify this task");
        }

        EtaExtension request = getEtaExtensionById(requestId);
        Task task = request.getTask();

        if (Boolean.TRUE.equals(task.getIsDeleted())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Task has been deleted");
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

        request.setStatus("APPROVED");
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(securityUtils.getCurrentUser());

        task.setEtaDate(request.getNewEtaDate());
        task.setExtendedEtaDate(request.getNewEtaDate());
        taskRepository.save(task);

        taskStatusHistoryService.createTaskStatusHistory(
                task,
                task.getStatus(),
                task.getStatus(),
                securityUtils.getCurrentUser(),
                "ETA extended from "
                        + request.getOldEtaDate()
                        + " to "
                        + request.getNewEtaDate()
                );

        return etaExtensionRepository.save(request);
    }

    @Transactional
    public EtaExtension rejectEtaExtensionRequest(Long requestId, String reason){

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN") && !currentEmployee.getRoles().contains("TEAM_LEAD") && !currentEmployee.getRoles().contains("SUB_LEAD")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not allowed to modify this task");
        }

        EtaExtension request = getEtaExtensionById(requestId);

        if (!"PENDING".equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request already processed");
        }
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejection reason is required");
        }

        request.setStatus("REJECTED");
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(securityUtils.getCurrentUser());
        request.setRejectionReason(reason);

        taskStatusHistoryService.createTaskStatusHistory(
                request.getTask(),
                request.getTask().getStatus(),
                request.getTask().getStatus(),
                securityUtils.getCurrentUser(),
                "ETA extension request rejected: " + reason
        );

        return etaExtensionRepository.save(request);
    }

    @Transactional
    public EtaExtension undoDecision(Long requestId){

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN") && !currentEmployee.getRoles().contains("TEAM_LEAD") && !currentEmployee.getRoles().contains("SUB_LEAD")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not allowed to modify this task");
        }

        EtaExtension request = getEtaExtensionById(requestId);
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
            if (task.getAssignedTo() == null ||
                    !task.getAssignedTo().getId().equals(request.getRequestedBy().getId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Task is no longer assigned to the requesting employee");
            }
            task.setEtaDate(request.getOldEtaDate());
            task.setExtendedEtaDate(request.getOldEtaDate().equals(task.getOriginalEtaDate()) ? null : request.getOldEtaDate());
            taskRepository.save(task);

            taskStatusHistoryService.createTaskStatusHistory(
                    task,
                    task.getStatus(),
                    task.getStatus(),
                    securityUtils.getCurrentUser(),
                    "Undid ETA extension approval. Reverted ETA to " + request.getOldEtaDate()
            );
        } else if ("REJECTED".equals(request.getStatus())) {
            taskStatusHistoryService.createTaskStatusHistory(
                    task,
                    task.getStatus(),
                    task.getStatus(),
                    securityUtils.getCurrentUser(),
                    "Undid ETA extension rejection (original rejection reason: " + request.getRejectionReason() + ")"
            );
        }
        request.setStatus("PENDING");
        request.setReviewedAt(null);
        request.setReviewedBy(null);
        request.setRejectionReason(null);

        return etaExtensionRepository.save(request);
    }

    public List<EtaExtension> getEtaExtensionRequestsByTaskId(Long taskId){
        Task task = taskService.getTaskById(taskId);

        return etaExtensionRepository.findByTask(task);
    }
}
