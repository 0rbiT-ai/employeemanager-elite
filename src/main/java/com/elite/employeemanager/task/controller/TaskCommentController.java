package com.elite.employeemanager.task.controller;

import com.elite.employeemanager.task.entity.TaskComment;
import com.elite.employeemanager.task.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/task-comments")
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    @PostMapping
    public ResponseEntity<TaskComment> addTaskComment(@RequestBody TaskComment comment){
        return new ResponseEntity<>(taskCommentService.addTaskComment(comment), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTaskCommentById(@PathVariable Long id){
        taskCommentService.deleteTaskCommentById(id);
        return new ResponseEntity<>("Comment Deleted Successfully",HttpStatus.OK);
    }

}
