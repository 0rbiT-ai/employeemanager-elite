package com.elite.employeemanager.employee.controller;

import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.service.EmployeeService;
import com.elite.employeemanager.team.entity.Team;
import com.elite.employeemanager.team.service.TeamEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    private final TeamEmployeeService teamEmployeeService;

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_MANAGE')")
    public ResponseEntity<Employee> addEmployee(@RequestBody Employee employee){
        return new ResponseEntity<>(employeeService.addEmployee(employee),HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_MANAGE')")
    public ResponseEntity<List<Employee>> getAllEmployees(){
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_MANAGE')")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id){
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_MANAGE')")
    public ResponseEntity<Employee> updateEmployeeById(@PathVariable Long id, @RequestBody Employee updatedEmployee){
        return ResponseEntity.ok(employeeService.updateEmployeeById(id,updatedEmployee));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_MANAGE')")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable Long id,
                                                     @RequestBody String reason){
        employeeService.deleteEmployeeById(id,reason);
        return new ResponseEntity<>("Employee Deleted",HttpStatus.OK);
    }

    @GetMapping("/{id}/teams")
    @PreAuthorize("hasAuthority('EMPLOYEE_MANAGE') or hasAuthority('TEAM_MANAGE')")
    public ResponseEntity<List<Team>> getTeamsByEmployeeId(@PathVariable Long id){
        return new ResponseEntity<>(teamEmployeeService.getTeamsByEmployeeId(id), HttpStatus.OK);
    }
}
