package com.elite.employeemanager.auth.mapping.service;

import com.elite.employeemanager.auth.mapping.entity.UserRole;
import com.elite.employeemanager.auth.mapping.repository.UserRoleRepository;
import com.elite.employeemanager.auth.role.entity.Role;
import com.elite.employeemanager.auth.role.repository.RoleRepository;
import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.employee.entity.Employee;
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
public class UserRoleRecalculationService {

    private final TeamRepository teamRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public void recalculateUserRoles(Employee employee){
        if (employee == null || employee.getUser() == null) {
            return;
        }
        User user = employee.getUser();
        boolean isLead = teamRepository.existsByLeadAndStatus(employee, "ACTIVE");
        boolean isSubLead = teamRepository.existsBySubLeadAndStatus(employee, "ACTIVE");

        List<UserRole> currentMappings = userRoleRepository.findByUser(user);

        boolean hasTeamLeadRole = currentMappings.stream()
                .anyMatch(userRole -> "TEAM_LEAD".equals(userRole.getRole().getRoleCode()));
        boolean hasSubLeadRole = currentMappings.stream()
                .anyMatch(userRole -> "SUB_LEAD".equals(userRole.getRole().getRoleCode()));

        if (isLead && !hasTeamLeadRole){
            Role teamLeadRole = roleRepository.findByRoleCode("TEAM_LEAD")
                    .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Role Not Found"));
            userRoleRepository.save(UserRole.builder()
                            .user(user)
                            .role(teamLeadRole)
                            .assignedAt(LocalDateTime.now())
                            .build()
            );
        } else if (!isLead && hasTeamLeadRole) {
            currentMappings.stream()
                    .filter(userRole -> "TEAM_LEAD".equals(userRole.getRole().getRoleCode()))
                    .forEach(userRoleRepository::delete);
        }

        if (isSubLead && !hasSubLeadRole){
            Role subLeadRole = roleRepository.findByRoleCode("SUB_LEAD")
                    .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Role Not Found"));
            userRoleRepository.save(UserRole.builder()
                    .user(user)
                    .role(subLeadRole)
                    .assignedAt(LocalDateTime.now())
                    .build()
            );
        } else if (!isSubLead && hasSubLeadRole) {
            currentMappings.stream()
                    .filter(userRole -> "SUB_LEAD".equals(userRole.getRole().getRoleCode()))
                    .forEach(userRoleRepository::delete);
        }
    }
}
