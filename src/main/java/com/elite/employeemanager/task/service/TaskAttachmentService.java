package com.elite.employeemanager.task.service;
import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.s3aws.service.S3Service;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.entity.TaskAttachment;
import com.elite.employeemanager.task.repository.TaskAttachmentRepository;
import com.elite.employeemanager.task.repository.TaskRepository;
import com.elite.employeemanager.team.entity.TeamEmployee;
import com.elite.employeemanager.team.repository.TeamEmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskAttachmentService {

    private final TaskAttachmentRepository taskAttachmentRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final EmployeeRepository employeeRepository;
    private final S3Service s3Service;
    private final SecurityUtils securityUtils;
    private final TeamEmployeeRepository teamEmployeeRepository;

    private final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    @Transactional
    public TaskAttachment uploadAttachment(Long taskId, MultipartFile file){

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds the maximum limit of 50MB");
        }

        Task task = taskService.getTaskById(taskId);

        Employee employee = securityUtils.getCurrentEmployee();

        // here maybe validate if user can upload file

        String originalFileName = Objects.requireNonNull(file.getOriginalFilename(), "Filename cannot be null");

        String key = "tasks/"+task.getTaskNumber()+"/"+ UUID.randomUUID()+"-"+originalFileName;
        try {
            s3Service.uploadFile(file,key);
        }catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Failed to Upload File",e);
        }

        TaskAttachment attachment =
                TaskAttachment.builder()
                        .task(task)
                        .fileName(originalFileName)
                        .filePath(key)
                        .fileSizeBytes(file.getSize())
                        .uploadedBy(employee)
                        .uploadedAt(LocalDateTime.now())
                        .build();
        try{
            return taskAttachmentRepository.saveAndFlush(attachment);
        }catch (Exception e){
            s3Service.deleteFile(key);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ResponseInputStream<GetObjectResponse> downloadAttachment(Long attachmentId, Long taskId){

        TaskAttachment attachment = getAttachment(attachmentId,taskId);

        Employee employee = securityUtils.getCurrentEmployee();

        return s3Service.downloadFileStream(attachment.getFilePath());
    }

    @Transactional
    public void deleteAttachment(Long attachmentId, Long taskId){

        TaskAttachment attachment = getAttachment(attachmentId,taskId);

        Employee employee = securityUtils.getCurrentEmployee();
        boolean isAdmin = employee.getRoles().contains("ADMIN");
        boolean isUploader = attachment.getUploadedBy().getId().equals(employee.getId());
        List<TeamEmployee> uploaderTeams = teamEmployeeRepository.findByEmployee(attachment.getUploadedBy());
        boolean isUploaderLead = uploaderTeams.stream()
                .map(TeamEmployee::getTeam)
                .anyMatch(team ->
                        (team.getLead() != null && team.getLead().getId().equals(employee.getId())) || (team.getSubLead() != null && team.getSubLead().getId().equals(employee.getId()))
                );

        if (!isAdmin && !isUploader && !isUploaderLead) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete attachments that you uploaded");
        }

        s3Service.deleteFile(attachment.getFilePath());
        taskAttachmentRepository.delete(attachment);
    }

    @Transactional(readOnly = true)
    public List<TaskAttachment> getAttachmentsByTaskId(Long taskId){
        Task task = taskService.getTaskById(taskId);
        return taskAttachmentRepository.findByTask(task);
    }

    @Transactional(readOnly = true)
    public TaskAttachment getAttachment(Long attachmentId, Long taskId) {
        taskService.getTaskById(taskId);
        return taskAttachmentRepository.findByIdAndTaskId(attachmentId, taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment Not Found"));
    }

}
