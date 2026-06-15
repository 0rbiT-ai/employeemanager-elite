package com.elite.employeemanager.task.service;

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

    @Transactional
    public TaskTagMapping addTagToTask(Long taskId, Long tagId){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task Not Found"));

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
        Task task = taskRepository.findById(taskId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task Not Found"));

        List<TaskTagMapping> mapping = taskTagMappingRepository.findByTask(task);

        return mapping.stream().map(TaskTagMapping::getTag).toList();
    }

    @Transactional
    public void removeTagFromTask(Long taskId,Long tagId){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task Not Found"));

        TaskTag tag = taskTagRepository.findById(tagId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task Tag Not Found"));

        if (!taskTagMappingRepository.existsByTaskAndTag(task,tag)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tag not added to Task");
        }

        taskTagMappingRepository.deleteByTaskAndTag(task,tag);
    }
}
