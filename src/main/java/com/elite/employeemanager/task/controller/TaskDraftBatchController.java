package com.elite.employeemanager.task.controller;

import com.elite.employeemanager.task.dto.TaskDraftRequest;
import com.elite.employeemanager.task.entity.TaskDraftBatch;
import com.elite.employeemanager.task.service.TaskDraftBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/task-drafts")
@RequiredArgsConstructor
public class TaskDraftBatchController {
    private final TaskDraftBatchService taskDraftBatchService;

    @PostMapping
    @PreAuthorize("hasAuthority('TASK_CREATE')")
    public ResponseEntity<String> saveDraft(@RequestBody TaskDraftRequest request) {
        taskDraftBatchService.saveDraft(request.getTeamsMessage(), request.getTeamsGroupId(), request.getTeamsChannelId());
        return new ResponseEntity<>("Task draft saved successfully", HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TASK_CREATE')")
    public ResponseEntity<TaskDraftBatch> getMyDraft() {
        return taskDraftBatchService.getMyDraft()
                .map(draft -> new ResponseEntity<>(draft, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('TASK_DELETE') or hasAuthority('TASK_UPDATE')")
    public ResponseEntity<String> deleteDraft() {
        taskDraftBatchService.deleteDraft();
        return new ResponseEntity<>("Task draft discarded successfully", HttpStatus.OK);
    }

    @PostMapping("/send")
    @PreAuthorize("hasAuthority('TEAMS_POST')")
    public ResponseEntity<String> sendToTeams() {
        taskDraftBatchService.sendToTeams();
        return new ResponseEntity<>("Task summary sent to Microsoft Teams successfully", HttpStatus.OK);
    }
}
