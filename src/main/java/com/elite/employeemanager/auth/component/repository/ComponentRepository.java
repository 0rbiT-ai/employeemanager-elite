package com.elite.employeemanager.auth.component.repository;

import com.elite.employeemanager.auth.component.entity.Component;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComponentRepository extends JpaRepository<Component,Long> {
}
