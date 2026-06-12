package com.elite.employeemanager.task.controller;

import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.entity.TaskComment;
import com.elite.employeemanager.task.entity.TaskStatusHistory;
import com.elite.employeemanager.task.service.TaskCommentService;
import com.elite.employeemanager.task.service.TaskService;
import com.elite.employeemanager.task.service.TaskStatusHistoryService;
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

    @PostMapping
    public ResponseEntity<Task> addTask(@RequestBody Task task){
        return new ResponseEntity<>(taskService.createTask(task), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(){
        return new ResponseEntity<>(taskService.getAllTasks(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id){
        return new ResponseEntity<>(taskService.getTaskById(id),HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Task> updateTaskById(@PathVariable Long id, @RequestBody Task task){
        return new ResponseEntity<>(taskService.updateTaskById(id,task),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTaskById(@PathVariable Long id, @RequestBody String reason){
        taskService.deleteTaskById(id,reason);
        return new ResponseEntity<>("Task Deleted Successfully",HttpStatus.OK);
    }

    @PatchMapping("/{id}/unassign")
    public ResponseEntity<String> unassignTaskById(@PathVariable Long id){
        taskService.unassignTaskById(id);
        return new ResponseEntity<>("Task unassigned",HttpStatus.OK);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<TaskComment>> getTaskCommentsByTaskId(@PathVariable Long id){
        return new ResponseEntity<>(taskCommentService.getTaskCommentsByTaskId(id),HttpStatus.OK);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<TaskStatusHistory>> getTaskStatusHistoryByTaskId(@PathVariable Long id) {
        return new ResponseEntity<>(taskStatusHistoryService.getTaskStatusHistoryByTaskId(id), HttpStatus.OK);
    }

}
