package com.elite.employeemanager.s3aws.health;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;

@Component
@RequiredArgsConstructor
public class S3HealthCheck {

    private final S3Client s3Client;

    @PostConstruct
    public void check() {
        System.out.println(
                s3Client.listBuckets()
                        .buckets()
                        .stream()
                        .map(Bucket::name)
                        .toList()
        );
    }
}