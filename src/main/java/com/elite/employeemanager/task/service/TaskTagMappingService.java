package com.elite.employeemanager.task.service;

import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.entity.TaskTag;
import com.elite.employeemanager.task.entity.TaskTagMapping;
import com.elite.employeemanager.task.repository.TaskRepository;
import com.elite.employeemanager.task.repository.TaskTagMappingRepository;
import com.elite.employeemanager.task.repository.TaskTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskTagMappingService {

    private final TaskTagRepository taskTagRepository;
    private final TaskTagMappingRepository taskTagMappingRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final SecurityUtils securityUtils;

    @Transactional
    public TaskTagMapping addTagToTask(Long taskId, Long tagId){

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN") && !currentEmployee.getRoles().contains("TEAM_LEAD") && !currentEmployee.getRoles().contains("SUB_LEAD")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not allowed to modify this task");
        }

        Task task = taskService.getTaskById(taskId);

        TaskTag tag = taskTagRepository.findById(tagId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task Tag Not Found"));

        if (taskTagMappingRepository.existsByTaskAndTag(task,tag)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tag already added to Task");
        }

        TaskTagMapping mapping = TaskTagMapping.builder()
                .tag(tag)
                .task(task)
                .build();
        return taskTagMappingRepository.save(mapping);
    }

    public List<TaskTag> getTagsByTaskId(Long taskId){
        Task task = taskService.getTaskById(taskId);

        List<TaskTagMapping> mapping = taskTagMappingRepository.findByTask(task);

        return mapping.stream().map(TaskTagMapping::getTag).toList();
    }

    @Transactional
    public void removeTagFromTask(Long taskId,Long tagId){

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN") && !currentEmployee.getRoles().contains("TEAM_LEAD") && !currentEmployee.getRoles().contains("SUB_LEAD")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not allowed to modify this task");
        }

        Task task = taskService.getTaskById(taskId);

        TaskTag tag = taskTagRepository.findById(tagId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task Tag Not Found"));

        if (!taskTagMappingRepository.existsByTaskAndTag(task,tag)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tag not added to Task");
        }

        taskTagMappingRepository.deleteByTaskAndTag(task,tag);
    }
}
