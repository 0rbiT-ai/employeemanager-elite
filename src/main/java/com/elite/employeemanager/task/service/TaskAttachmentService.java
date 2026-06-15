package com.elite.employeemanager.task.service;

import com.elite.employeemanager.task.repository.TaskAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskAttachmentService {
    private final TaskAttachmentRepository taskAttachmentRepository;
}
