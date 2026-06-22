package com.elite.employeemanager.task.service;

import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.task.entity.TaskTag;
import com.elite.employeemanager.task.repository.TaskTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskTagService {

    private final TaskTagRepository taskTagRepository;
    private final SecurityUtils securityUtils;

    public TaskTag createTaskTag(TaskTag tag){

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN") && !currentEmployee.getRoles().contains("TEAM_LEAD") && !currentEmployee.getRoles().contains("SUB_LEAD")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not allowed create tags");
        }

        if (tag.getTagName()==null||tag.getTagName().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tag Name is required");
        }
        if (taskTagRepository.existsByTagName(tag.getTagName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tag already exists");
        }
        tag.setCreatedAt(LocalDateTime.now());
        return taskTagRepository.save(tag);
    }

    public List<TaskTag> getAllTaskTags(){
        return taskTagRepository.findAll();
    }

    public TaskTag getTaskTagById(Long id){
        return taskTagRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Tag Not Found"));
    }

    public void deleteTaskTagById(Long id){
        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN") && !currentEmployee.getRoles().contains("TEAM_LEAD") && !currentEmployee.getRoles().contains("SUB_LEAD")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not allowed delete tags");
        }
        TaskTag taskTag = getTaskTagById(id);
        taskTagRepository.delete(taskTag);
    }

}
