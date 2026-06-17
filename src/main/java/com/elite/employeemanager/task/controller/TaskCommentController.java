package com.elite.employeemanager.task.controller;

import com.elite.employeemanager.task.entity.TaskComment;
import com.elite.employeemanager.task.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/task-comments")
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    @PostMapping
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<TaskComment> addTaskComment(@RequestBody TaskComment comment){
        return new ResponseEntity<>(taskCommentService.addTaskComment(comment), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<String> deleteTaskCommentById(@PathVariable Long id){
        taskCommentService.deleteTaskCommentById(id);
        return new ResponseEntity<>("Comment Deleted Successfully",HttpStatus.OK);
    }

}
