package com.elite.employeemanager.team.repository;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team,Long> {
    boolean existsByLead(Employee employee);
    boolean existsBySubLead(Employee employee);

    boolean existsByLeadAndStatus(Employee employee, String status);

    boolean existsBySubLeadAndStatus(Employee subLead, String status);

    List<Team> findByLead(Employee employee);
    List<Team> findBySubLead(Employee employee);

}
