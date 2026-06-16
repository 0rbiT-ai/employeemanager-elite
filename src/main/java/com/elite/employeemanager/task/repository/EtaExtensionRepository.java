package com.elite.employeemanager.task.repository;

import com.elite.employeemanager.task.entity.EtaExtension;
import com.elite.employeemanager.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtaExtensionRepository extends JpaRepository<EtaExtension, Long> {
    boolean existsByTaskAndStatus(Task task, String status);

    List<EtaExtension> findByTask(Task task);
}
