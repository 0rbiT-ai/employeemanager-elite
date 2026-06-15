package com.elite.employeemanager.task.repository;

import com.elite.employeemanager.task.entity.TaskTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskTagRepository extends JpaRepository<TaskTag,Long> {
    boolean existsByTagName(String tagName);

    Optional<TaskTag> findByTagName(String tagName);
}
