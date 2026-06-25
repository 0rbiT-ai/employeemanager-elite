package com.elite.employeemanager.meeting.attachment.controller;

import com.elite.employeemanager.meeting.attachment.entity.Attachment;
import com.elite.employeemanager.meeting.attachment.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ATTACHMENT_UPLOAD')")
    public ResponseEntity<Attachment> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("meetingId") Long meetingId) {
        return new ResponseEntity<>(attachmentService.uploadAttachment(file, meetingId), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTACHMENT_VIEW')")
    public ResponseEntity<Attachment> getAttachmentById(@PathVariable Long id) {
        return new ResponseEntity<>(attachmentService.getAttachmentById(id), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ATTACHMENT_VIEW')")
    public ResponseEntity<List<Attachment>> getAllAttachments() {
        return new ResponseEntity<>(attachmentService.getAllAttachments(), HttpStatus.OK);
    }


    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('ATTACHMENT_VIEW')")
    public ResponseEntity<InputStreamResource> downloadAttachment(@PathVariable Long id) {
        Attachment attachment = attachmentService.getAttachmentById(id);
        ResponseInputStream<GetObjectResponse> s3Stream = attachmentService.downloadAttachment(id);
        GetObjectResponse s3Response = s3Stream.response();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        s3Response.contentType() != null ? s3Response.contentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .contentLength(s3Response.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(new InputStreamResource(s3Stream));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTACHMENT_DELETE')")
    public ResponseEntity<String> deleteAttachment(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);
        return new ResponseEntity<>("Attachment deleted successfully", HttpStatus.OK);
    }
}
