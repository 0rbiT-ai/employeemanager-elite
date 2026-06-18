package com.elite.employeemanager.team.service;

import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.team.entity.Team;
import com.elite.employeemanager.team.entity.TeamEmployee;
import com.elite.employeemanager.team.repository.TeamEmployeeRepository;
import com.elite.employeemanager.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamEmployeeService {
    private final TeamEmployeeRepository teamEmployeeRepository;
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public TeamEmployee addEmployeeToTeam(Long teamId, Long employeeId){

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Team Not Found"));

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN")){
            boolean isLead = team.getLead()!=null && team.getLead().getId().equals(currentEmployee.getId());
            boolean isSubLead = team.getSubLead()!=null && team.getSubLead().getId().equals(currentEmployee.getId());
            if (!isLead && !isSubLead){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Current User not allowed to assign members to team");
            }
        }

        if (teamEmployeeRepository.findByTeamAndEmployee(team,employee).isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Employee already belongs to this team");
        }

        TeamEmployee teamEmployee = TeamEmployee.builder()
                .employee(employee)
                .team(team)
                .joinedAt(LocalDateTime.now())
                .build();

        return teamEmployeeRepository.save(teamEmployee);
    }

    @Transactional
    public void removeEmployeeFromTeam(Long teamId, Long employeeId){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Team Not Found"));

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN")){
            boolean isLead = team.getLead()!=null && team.getLead().getId().equals(currentEmployee.getId());
            boolean isSubLead = team.getSubLead()!=null && team.getSubLead().getId().equals(currentEmployee.getId());
            if (!isLead && !isSubLead){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Current User not allowed to assign members to team");
            }
        }

        TeamEmployee teamEmployee = teamEmployeeRepository.findByTeamAndEmployee(team,employee)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee does not belong to this team"));

        teamEmployeeRepository.delete(teamEmployee);
    }

    public List<Employee> getMembersByTeamId(Long teamId){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Team Not Found"));

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN")){
            boolean isLead = team.getLead() != null && team.getLead().getId().equals(currentEmployee.getId());
            boolean isSubLead = team.getSubLead() != null && team.getSubLead().getId().equals(currentEmployee.getId());
            boolean isMember = teamEmployeeRepository.findByTeamAndEmployee(team, currentEmployee).isPresent();
            if (!isMember && !isLead && !isSubLead){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Current User does not belong to this Team");
            }
        }

        List<TeamEmployee> teamEmployees = teamEmployeeRepository.findByTeam(team);

        return teamEmployees.stream().map(TeamEmployee::getEmployee).toList();
    }

    public List<Team> getTeamsByEmployeeId(Long employeeId){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN") && !currentEmployee.getId().equals(employeeId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Current User does not belong to this Team");
        }

        List<TeamEmployee> teamEmployees = teamEmployeeRepository.findByEmployee(employee);

        return teamEmployees.stream().map(TeamEmployee::getTeam).toList();
    }
}
