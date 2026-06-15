package com.elite.employeemanager.task.controller;

import com.elite.employeemanager.task.entity.TaskTagMapping;
import com.elite.employeemanager.task.service.TaskTagMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskTagMappingController {

    private final TaskTagMappingService taskTagMappingService;

    @PostMapping("/{taskId}/tags/{tagId}")
    public ResponseEntity<TaskTagMapping> addTagToTask(@PathVariable Long taskId, @PathVariable Long tagId){
        return new ResponseEntity<>(taskTagMappingService.addTagToTask(taskId,tagId), HttpStatus.CREATED);
    }

    @DeleteMapping("/{taskId}/tags/{tagId}")
    public ResponseEntity<String> removeTagFromTask(@PathVariable Long taskId, @PathVariable Long tagId){
        taskTagMappingService.removeTagFromTask(taskId,tagId);
        return new ResponseEntity<>("Tag removed from Task Successfully",HttpStatus.OK);
    }

}
