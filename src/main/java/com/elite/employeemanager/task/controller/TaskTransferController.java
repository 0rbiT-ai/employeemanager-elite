package com.elite.employeemanager.task.controller;

import com.elite.employeemanager.task.entity.TaskTransfer;
import com.elite.employeemanager.task.service.TaskTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/task-transfers")
@RequiredArgsConstructor
public class TaskTransferController {

    private final TaskTransferService taskTransferService;

    @PostMapping
    public ResponseEntity<TaskTransfer> createTaskTransferRequest(@RequestBody TaskTransfer taskTransfer){
        return new ResponseEntity<>(taskTransferService.createTaskTransferRequest(taskTransfer), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskTransfer> getTaskTransferById(@PathVariable Long id){
        return new ResponseEntity<>(taskTransferService.getTaskTransferById(id),HttpStatus.OK);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<TaskTransfer> approveTaskTransferRequest(@PathVariable Long id){
        return new ResponseEntity<>(taskTransferService.approveTaskTransferRequest(id),HttpStatus.OK);
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<TaskTransfer> rejectTaskTransferRequest(@PathVariable Long id, @RequestBody String reason){
        return new ResponseEntity<>(taskTransferService.rejectTaskTransferRequest(id,reason),HttpStatus.OK);
    }

    @PatchMapping("/{id}/undo")
    public ResponseEntity<TaskTransfer> undoDecision(@PathVariable Long id){
        return new ResponseEntity<>(taskTransferService.undoDecision(id),HttpStatus.OK);
    }
}
