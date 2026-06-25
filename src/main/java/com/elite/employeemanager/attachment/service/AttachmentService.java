package com.elite.employeemanager.attachment.service;

import com.elite.employeemanager.attachment.entity.Attachment;
import com.elite.employeemanager.attachment.repository.AttachmentRepository;
import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.s3aws.service.S3Service;
import com.elite.employeemanager.team.entity.TeamEmployee;
import com.elite.employeemanager.team.repository.TeamEmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final S3Service s3Service;
    private final SecurityUtils securityUtils;
    private final TeamEmployeeRepository teamEmployeeRepository;
    private final long MAX_FILE_SIZE = 50*1024*1024;

    @Transactional
    public Attachment uploadAttachment(MultipartFile file){
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds the maximum limit of 50MB");
        }
        Employee employee = securityUtils.getCurrentEmployee();
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename(), "Filename cannot be null");
        String key = "attachments/"+ UUID.randomUUID()+"-"+originalFileName;
        try {
            s3Service.uploadFile(file,key);
        }catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Failed to Upload File",e);
        }
        Attachment attachment = Attachment.builder()
                .fileName(originalFileName)
                .filePath(key)
                .fileSizeBytes(file.getSize())
                .uploadedBy(employee)
                .uploadedAt(LocalDateTime.now())
                .build();
        try{
            return attachmentRepository.saveAndFlush(attachment);
        }catch (Exception e){
            s3Service.deleteFile(key);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ResponseInputStream<GetObjectResponse> downloadAttachment(long attachmentId){
        Attachment attachment = getAttachmentById(attachmentId);
        Employee employee = securityUtils.getCurrentEmployee();
        return s3Service.downloadFileStream(attachment.getFilePath());
    }

    @Transactional
    public void deleteAttachment(Long attachmentId){

        Attachment attachment = getAttachmentById(attachmentId);

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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this attachment");
        }

        s3Service.deleteFile(attachment.getFilePath());
        attachmentRepository.delete(attachment);
    }

    @Transactional(readOnly = true)
    public Attachment getAttachmentById(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment Not Found"));
    }

    @Transactional(readOnly = true)
    public List<Attachment> getAllAttachments(){
        return attachmentRepository.findAll();
    }

}
