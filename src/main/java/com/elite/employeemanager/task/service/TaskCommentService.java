package com.elite.employeemanager.task.service;

import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.entity.TaskComment;
import com.elite.employeemanager.task.repository.TaskCommentRepository;
import com.elite.employeemanager.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;

    private User getCurrentUser(){
        Object principal = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if(principal instanceof User user) {
            return user;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }

    public TaskComment addTaskComment(TaskComment comment){

        if (comment.getCommentText()==null||comment.getCommentText().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Comment Text is required");
        }

        if (comment.getAuthor()==null || comment.getAuthor().getId()==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Author Id is required");
        }

        if (comment.getTask()==null || comment.getTask().getId()==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Task Id is required");
        }

        User user = getCurrentUser();
        Employee author = employeeRepository.findByWorkEmail(user.getEmail())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Author not found"));

        Task task = taskRepository.findById(comment.getTask().getId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task not found"));

        comment.setTask(task);
        comment.setAuthor(author);
        return taskCommentRepository.save(comment);
    }

    public List<TaskComment> getTaskCommentsByTaskId(Long taskId){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task not found"));
        return taskCommentRepository.findByTask(task);
    }

    public void deleteTaskCommentById(Long id){
        TaskComment comment = taskCommentRepository.findById(id)
                        .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Comment not found"));
        taskCommentRepository.delete(comment);
    }
}
