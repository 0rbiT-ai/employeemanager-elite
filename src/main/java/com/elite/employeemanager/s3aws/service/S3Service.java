package com.elite.employeemanager.s3aws.service;

import com.elite.employeemanager.s3aws.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public String uploadFile(MultipartFile file, String key) throws IOException {

        if (file.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"File cannot be empty.");
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(key)
                .build();
        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return key;
    }

    public ResponseInputStream<GetObjectResponse> downloadFileStream(String key){
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(key)
                .build();

        return s3Client.getObject(request);
    }

    public void deleteFile(String key){
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }
}
