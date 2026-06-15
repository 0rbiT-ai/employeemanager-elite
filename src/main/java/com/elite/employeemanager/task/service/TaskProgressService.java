package com.elite.employeemanager.task.service;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.project.repository.ProjectEmployeeRepository;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.entity.TaskProgress;
import com.elite.employeemanager.task.repository.TaskProgressRepository;
import com.elite.employeemanager.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskProgressService {

    private final TaskProgressRepository taskProgressRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ProjectEmployeeRepository projectEmployeeRepository;

    @Transactional
    public TaskProgress addTaskProgress(TaskProgress progress){
        if (progress.getTask() == null || progress.getTask().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task Id is required");
        }

        if (progress.getEmployee() == null || progress.getEmployee().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee Id is required");
        }

        if (progress.getProgressPercentage() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Progress Percentage is required");
        }
        if (progress.getProgressPercentage()<0 || progress.getProgressPercentage()>100){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Progress Percentage exceeds valid range");
        }
        Task task = taskRepository.findById(progress.getTask().getId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task Not Found"));
        Employee employee = employeeRepository.findById(progress.getEmployee().getId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));
        projectEmployeeRepository.findByProjectAndEmployee(task.getProject(), employee)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Employee does not belong to the Project of this Task"));
        if (!employee.equals(task.getAssignedTo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Task not assigned to current employee");
        }

        progress.setTask(task);
        progress.setEmployee(employee);
        progress.setCreatedAt(LocalDateTime.now());
        return taskProgressRepository.save(progress);
    }

    public List<TaskProgress> getTaskProgressByTaskId(Long taskId){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task Not Found"));
        return taskProgressRepository.findByTask(task);
    }

    public TaskProgress getTaskProgressById(Long id){
        return taskProgressRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task Progress Not Found"));
    }

    public void deleteTaskProgressById(Long id){
        TaskProgress taskProgress = getTaskProgressById(id);
        taskProgressRepository.delete(taskProgress);
    }
}
