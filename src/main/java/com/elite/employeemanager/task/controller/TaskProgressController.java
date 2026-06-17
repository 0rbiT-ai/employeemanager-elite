package com.elite.employeemanager.task.controller;

import com.elite.employeemanager.task.entity.TaskProgress;
import com.elite.employeemanager.task.service.TaskProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/task-progress")
@RequiredArgsConstructor
public class TaskProgressController {

    private final TaskProgressService taskProgressService;

    @PostMapping
    @PreAuthorize("hasAuthority('TASK_VIEW')")
    public ResponseEntity<TaskProgress> addTaskProgress(@RequestBody TaskProgress taskProgress){
        return new ResponseEntity<>(taskProgressService.addTaskProgress(taskProgress), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TASK_UPDATE')")
    public ResponseEntity<String> deleteTaskProgress(@PathVariable Long id){
        taskProgressService.deleteTaskProgressById(id);
        return new ResponseEntity<>("Task Progress Deleted",HttpStatus.OK);
    }

}
