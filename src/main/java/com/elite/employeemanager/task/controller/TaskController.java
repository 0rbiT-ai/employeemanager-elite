package com.elite.employeemanager.task.controller;

import com.elite.employeemanager.task.entity.*;
import com.elite.employeemanager.task.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskCommentService taskCommentService;
    private final TaskStatusHistoryService taskStatusHistoryService;
    private final TaskProgressService taskProgressService;
    private final TaskTagMappingService taskTagMappingService;
    private final TaskAttachmentService taskAttachmentService;
    private final EtaExtensionService etaExtensionService;
    private final TaskTransferService taskTransferService;

    @PostMapping
    @PreAuthorize("hasAuthority('TASK_CREATE')")
    public ResponseEntity<Task> addTask(@RequestBody Task task){
        return new ResponseEntity<>(taskService.createTask(task), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<List<Task>> getAllTasks(){
        return new ResponseEntity<>(taskService.getAllTasks(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id){
        return new ResponseEntity<>(taskService.getTaskById(id),HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('TASK_UPDATE')")
    public ResponseEntity<Task> updateTaskById(@PathVariable Long id, @RequestBody Task task){
        return new ResponseEntity<>(taskService.updateTaskById(id,task),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TASK_DELETE')")
    public ResponseEntity<String> deleteTaskById(@PathVariable Long id, @RequestBody String reason){
        taskService.deleteTaskById(id,reason);
        return new ResponseEntity<>("Task Deleted Successfully",HttpStatus.OK);
    }

    @PatchMapping("/{id}/unassign")
    @PreAuthorize("hasAuthority('TASK_UPDATE')")
    public ResponseEntity<String> unassignTaskById(@PathVariable Long id){
        taskService.unassignTaskById(id);
        return new ResponseEntity<>("Task unassigned",HttpStatus.OK);
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<List<TaskComment>> getTaskCommentsByTaskId(@PathVariable Long id){
        return new ResponseEntity<>(taskCommentService.getTaskCommentsByTaskId(id),HttpStatus.OK);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<List<TaskStatusHistory>> getTaskStatusHistoryByTaskId(@PathVariable Long id) {
        taskService.getTaskById(id); //task visibility check
        return new ResponseEntity<>(taskStatusHistoryService.getTaskStatusHistoryByTaskId(id), HttpStatus.OK);
    }

    @GetMapping("/{id}/progress")
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<List<TaskProgress>> getTaskProgressByTaskId(@PathVariable Long id) {
        return new ResponseEntity<>(taskProgressService.getTaskProgressByTaskId(id), HttpStatus.OK);
    }

    @GetMapping("/{id}/tags")
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<List<TaskTag>> getTagsByTaskId(@PathVariable Long id){
        return new ResponseEntity<>(taskTagMappingService.getTagsByTaskId(id),HttpStatus.OK);
    }

    @GetMapping("/{id}/attachments")
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<List<TaskAttachment>> getAttachmentsByTaskId(@PathVariable Long id){
        return new ResponseEntity<>(taskAttachmentService.getAttachmentsByTaskId(id),HttpStatus.OK);
    }

    @GetMapping("/{id}/eta-extensions")
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<List<EtaExtension>> getEtaExtensionRequestsByTaskId(@PathVariable Long id){
        return new ResponseEntity<>(etaExtensionService.getEtaExtensionRequestsByTaskId(id),HttpStatus.OK);
    }

    @GetMapping("/{id}/task-transfers")
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<List<TaskTransfer>> getTaskTransferRequestsByTaskId(@PathVariable Long id){
        return new ResponseEntity<>(taskTransferService.getTaskTransferByTaskId(id),HttpStatus.OK);
    }
}
