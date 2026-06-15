package com.elite.employeemanager.task.repository;

import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.entity.TaskTag;
import com.elite.employeemanager.task.entity.TaskTagMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskTagMappingRepository extends JpaRepository<TaskTagMapping,Long> {

    boolean existsByTaskAndTag(Task task, TaskTag tag);

    List<TaskTagMapping> findByTask(Task task);

    void deleteByTaskAndTag(Task task, TaskTag tag);
}
