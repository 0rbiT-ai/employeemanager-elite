package com.elite.employeemanager.task.controller;

import com.elite.employeemanager.task.entity.TaskTag;
import com.elite.employeemanager.task.service.TaskTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/task-tags")
@RequiredArgsConstructor
public class TaskTagController {

    private final TaskTagService taskTagService;

    @PostMapping
    public ResponseEntity<TaskTag> createTaskTag(@RequestBody TaskTag taskTag){
        return new ResponseEntity<>(taskTagService.createTaskTag(taskTag), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TaskTag>> getAllTaskTags(){
        return new ResponseEntity<>(taskTagService.getAllTaskTags(),HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskTag> getTaskTagById(@PathVariable Long id){
        return new ResponseEntity<>(taskTagService.getTaskTagById(id),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTaskTagById(@PathVariable Long id){
        taskTagService.deleteTaskTagById(id);
        return new ResponseEntity<>("Task Tag deleted successfully",HttpStatus.OK);
    }

}
