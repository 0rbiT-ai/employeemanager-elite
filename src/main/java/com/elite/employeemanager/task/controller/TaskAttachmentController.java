package com.elite.employeemanager.task.controller;

import com.elite.employeemanager.task.entity.TaskAttachment;
import com.elite.employeemanager.task.repository.TaskAttachmentRepository;
import com.elite.employeemanager.task.service.TaskAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskAttachmentController {

    private final TaskAttachmentService taskAttachmentService;

    @PostMapping("/{taskId}/attachments")
    public ResponseEntity<TaskAttachment> uploadAttachment(@PathVariable Long taskId,
                                                           @RequestParam("file")MultipartFile file){

        return new ResponseEntity<>(taskAttachmentService.uploadAttachment(taskId,file), HttpStatus.CREATED);
    }

    @GetMapping("/{taskId}/attachments/{attachmentId}")
    public ResponseEntity<InputStreamResource> downloadAttachment(@PathVariable Long taskId, @PathVariable Long attachmentId){

        TaskAttachment attachment = taskAttachmentService.getAttachment(attachmentId,taskId);
        ResponseInputStream<GetObjectResponse> fileStream = taskAttachmentService.downloadAttachment(attachmentId,taskId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+attachment.getFileName()+"\"")
                .contentLength(attachment.getFileSizeBytes())
                .body(new InputStreamResource(fileStream));
    }

    @DeleteMapping("/{taskId}/attachments/{attachmentId}")
    public ResponseEntity<String> deleteAttachment(@PathVariable Long taskId, @PathVariable Long attachmentId){
        taskAttachmentService.deleteAttachment(attachmentId,taskId);
        return ResponseEntity.ok("File Deleted Successfully");
    }

}
