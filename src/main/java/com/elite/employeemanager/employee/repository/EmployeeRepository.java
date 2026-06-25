package com.elite.employeemanager.employee.repository;

import com.elite.employeemanager.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    Optional<Employee> findByWorkEmail(String workEmail);

    @Query(value = """
    SELECT *
    FROM employees
    WHERE work_email = :email
    """, nativeQuery = true)
    Optional<Employee> findAnyByWorkEmail(@Param("email") String email);

    Optional<Employee> findByUserId(Long userId);
}
