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

    private User getCurrentUser(){
        Object principal = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if(principal instanceof User user) {
            return user;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }

    public EtaExtension getEtaExtensionById(Long id){
        return etaExtensionRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"ETA Request Not Found"));
    }

    @Transactional
    public EtaExtension createEtaExtensionRequest(EtaExtension etaExtension){

        if (etaExtension.getTask() == null || etaExtension.getTask().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task Id is required");
        }

        Task task = taskRepository.findById(etaExtension.getTask().getId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task Not Found"));

        User user = getCurrentUser();
        Employee employee = employeeRepository.findByWorkEmail(user.getEmail())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));

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
        request.setReviewedBy(getCurrentUser());

        task.setEtaDate(request.getNewEtaDate());
        task.setExtendedEtaDate(request.getNewEtaDate());
        taskRepository.save(task);

        taskStatusHistoryService.createTaskStatusHistory(
                task,
                task.getStatus(),
                task.getStatus(),
                getCurrentUser(),
                "ETA extended from "
                        + request.getOldEtaDate()
                        + " to "
                        + request.getNewEtaDate()
                );

        return etaExtensionRepository.save(request);
    }

    @Transactional
    public EtaExtension rejectEtaExtensionRequest(Long requestId, String reason){
        EtaExtension request = getEtaExtensionById(requestId);

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
                "ETA extension request rejected: " + reason
        );

        return etaExtensionRepository.save(request);
    }

    @Transactional
    public EtaExtension undoDecision(Long requestId){
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
                    getCurrentUser(),
                    "Undid ETA extension approval. Reverted ETA to " + request.getOldEtaDate()
            );
        } else if ("REJECTED".equals(request.getStatus())) {
            taskStatusHistoryService.createTaskStatusHistory(
                    task,
                    task.getStatus(),
                    task.getStatus(),
                    getCurrentUser(),
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
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task Not Found"));

        return etaExtensionRepository.findByTask(task);
    }
}
