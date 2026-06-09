package com.elite.employeemanager.team.repository;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.team.entity.Team;
import com.elite.employeemanager.team.entity.TeamEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamEmployeeRepository extends JpaRepository<TeamEmployee, Long> {
    Optional<TeamEmployee> findByTeamAndEmployee(Team team,Employee employee);
    List<TeamEmployee> findByTeam(Team team);
    List<TeamEmployee> findByEmployee(Employee employee);

}
