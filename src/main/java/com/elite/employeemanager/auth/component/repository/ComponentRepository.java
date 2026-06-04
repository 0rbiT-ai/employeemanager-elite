package com.elite.employeemanager.auth.component.repository;

import com.elite.employeemanager.auth.component.entity.Component;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComponentRepository extends JpaRepository<Component,Long> {
    Optional<Component> findByComponentKey(String key);
}
