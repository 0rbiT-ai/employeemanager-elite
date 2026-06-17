package com.elite.employeemanager.team.controller;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.team.entity.Team;
import com.elite.employeemanager.team.entity.TeamEmployee;
import com.elite.employeemanager.team.service.TeamEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamEmployeeController {
    private final TeamEmployeeService teamEmployeeService;

    @PostMapping("/{teamId}/employees/{employeeId}")
    @PreAuthorize("hasAuthority('TEAM_UPDATE')")
    public ResponseEntity<TeamEmployee> addEmployeeToTeam(@PathVariable Long teamId,@PathVariable Long employeeId){
        return new ResponseEntity<>(teamEmployeeService.addEmployeeToTeam(teamId,employeeId), HttpStatus.CREATED);
    }

    @DeleteMapping("/{teamId}/employees/{employeeId}")
    @PreAuthorize("hasAuthority('TEAM_UPDATE')")
    public ResponseEntity<String> removeEmployeeFromTeam(@PathVariable Long teamId,@PathVariable Long employeeId){
        teamEmployeeService.removeEmployeeFromTeam(teamId,employeeId);
        return new ResponseEntity<>("Employee removed from team", HttpStatus.OK);
    }

    @GetMapping("/{teamId}/employees")
    @PreAuthorize("hasAuthority('TEAM_VIEW')")
    public ResponseEntity<List<Employee>> getMembersByTeamId(@PathVariable Long teamId){
        return new ResponseEntity<>(teamEmployeeService.getMembersByTeamId(teamId), HttpStatus.OK);
    }

}
