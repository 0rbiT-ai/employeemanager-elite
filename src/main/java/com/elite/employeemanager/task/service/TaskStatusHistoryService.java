package com.elite.employeemanager.task.service;

import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.entity.TaskStatusHistory;
import com.elite.employeemanager.task.repository.TaskRepository;
import com.elite.employeemanager.task.repository.TaskStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskStatusHistoryService {
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final TaskRepository taskRepository;

    public TaskStatusHistory createTaskStatusHistory(Task task,
                                                     String oldStatus,
                                                     String newStatus,
                                                     User changedBy,
                                                     String reason){
        TaskStatusHistory history = TaskStatusHistory.builder()
                .task(task)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .reason(reason)
                .build();
        return taskStatusHistoryRepository.save(history);
    }

    public List<TaskStatusHistory> getTaskStatusHistoryByTaskId(Long taskId){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task Not Found"));
        return taskStatusHistoryRepository.findByTask(task);
    }
}
