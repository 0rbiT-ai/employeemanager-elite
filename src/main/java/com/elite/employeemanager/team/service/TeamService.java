package com.elite.employeemanager.team.service;

import com.elite.employeemanager.auth.mapping.service.UserRoleRecalculationService;
import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.team.entity.Team;
import com.elite.employeemanager.team.entity.TeamEmployee;
import com.elite.employeemanager.team.repository.TeamEmployeeRepository;
import com.elite.employeemanager.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;
    private final TeamEmployeeRepository teamEmployeeRepository;
    private final UserRoleRecalculationService userRoleRecalculationService;
    private final SecurityUtils securityUtils;

    @Transactional
    public Team addTeam(Team team){

        Employee employee = securityUtils.getCurrentEmployee();
        if (!employee.getRoles().contains("ADMIN") && !employee.getRoles().contains("TEAM_LEAD") && !employee.getRoles().contains("SUB_LEAD")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not allowed to create teams");
        }

        if (team.getStatus() == null) {
            team.setStatus("ACTIVE");
        }

        if (team.getTeamsGroupId() == null || team.getTeamsGroupId().isBlank() ||
                team.getTeamsChannelId() == null || team.getTeamsChannelId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teams Group ID and Channel ID are required");
        }

        if (team.getLead() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Team lead is required"
            );
        }
        Employee lead = employeeRepository.findById(team.getLead().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee Not Found"));
        team.setLead(lead);

        Employee subLead = null;
        if (team.getSubLead() != null && team.getSubLead().getId() != null) {
            subLead = employeeRepository.findById(team.getSubLead().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sub-Lead Employee Not Found"));
        }
        team.setSubLead(subLead);

        if(subLead!=null && team.getSubLead().getId().equals(team.getLead().getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Team lead and Sub Lead cannot be the same");
        }

        Team savedTeam = teamRepository.save(team);
        userRoleRecalculationService.recalculateUserRoles(lead);
        if (subLead!=null){
            userRoleRecalculationService.recalculateUserRoles(subLead);
        }
        return savedTeam;
    }

    public List<Team> getAllTeams(){
        Employee employee = securityUtils.getCurrentEmployee();
        if (!employee.getRoles().contains("ADMIN")){
            List<Team> memberTeams = teamEmployeeRepository.findByEmployee(employee).stream()
                    .map(TeamEmployee::getTeam).toList();
            List<Team> leadTeams = teamRepository.findByLead(employee);
            List<Team> subLeadTeams = teamRepository.findBySubLead(employee);
            return Stream.of(memberTeams,leadTeams,subLeadTeams)
                    .flatMap(List::stream)
                    .distinct()
                    .toList();
        }
        return teamRepository.findAll();
    }

    public Team getTeamById(Long id){
        Team team = teamRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Team Not Found with Id : "+id));

        Employee employee = securityUtils.getCurrentEmployee();
        if (!employee.getRoles().contains("ADMIN")){
            boolean isLead = team.getLead() != null && team.getLead().getId().equals(employee.getId());
            boolean isSubLead = team.getSubLead() != null && team.getSubLead().getId().equals(employee.getId());
            boolean isMember = teamEmployeeRepository.findByTeamAndEmployee(team, employee).isPresent();
            if (!isMember && !isLead && !isSubLead){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Current User does not belong to this Team");
            }
        }
        return team;
    }

    @Transactional
    public Team updateTeamById(Long id, Team updatedTeam){



        Team existingTeam = getTeamById(id);
        Employee oldLead = existingTeam.getLead();
        Employee oldSubLead = existingTeam.getSubLead();

        Employee employee = securityUtils.getCurrentEmployee();
        if (!employee.getRoles().contains("ADMIN")){
            boolean isLead = existingTeam.getLead() != null && existingTeam.getLead().getId().equals(employee.getId());
            boolean isSubLead = existingTeam.getSubLead() != null && existingTeam.getSubLead().getId().equals(employee.getId());
            if (!isLead && !isSubLead){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Current User does not belong to this Team");
            }
        }

        if (updatedTeam.getTeamName()!=null){
            existingTeam.setTeamName(updatedTeam.getTeamName());
        }

        if (updatedTeam.getDescription()!=null){
            existingTeam.setDescription(updatedTeam.getDescription());
        }

        if (updatedTeam.getTeamsChannelId() != null) {
            if (updatedTeam.getTeamsChannelId().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teams Channel ID cannot be empty");
            }
            existingTeam.setTeamsChannelId(updatedTeam.getTeamsChannelId());
        }

        if (updatedTeam.getTeamsGroupId() != null) {
            if (updatedTeam.getTeamsGroupId().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teams Group ID cannot be empty");
            }
            existingTeam.setTeamsGroupId(updatedTeam.getTeamsGroupId());
        }

        Employee finalLead = updatedTeam.getLead() != null ? updatedTeam.getLead() : existingTeam.getLead();
        Employee finalSubLead = updatedTeam.getSubLead() != null ? updatedTeam.getSubLead() : existingTeam.getSubLead();
        if (finalLead != null && finalSubLead != null &&
                finalLead.getId().equals(finalSubLead.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team lead and sub lead cannot be the same");
        }

        if (updatedTeam.getLead()!=null){
            Employee lead = employeeRepository.findById(updatedTeam.getLead().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee Not Found"));
            existingTeam.setLead(lead);
        }

        if (updatedTeam.getSubLead()!=null){
            Employee subLead = employeeRepository.findById(updatedTeam.getSubLead().getId())
                    .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));
            existingTeam.setSubLead(subLead);
        }

        if (updatedTeam.getStatus()!=null){
            String status = updatedTeam.getStatus().trim().toUpperCase();
            if (!status.equals("ACTIVE") && !status.equals("INACTIVE")){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid Team Status");
            }
            existingTeam.setStatus(status);
        }

        Team savedTeam = teamRepository.save(existingTeam);
        userRoleRecalculationService.recalculateUserRoles(savedTeam.getLead());
        userRoleRecalculationService.recalculateUserRoles(savedTeam.getSubLead());

        if (oldLead != null && !oldLead.getId().equals(savedTeam.getLead().getId())) {
            userRoleRecalculationService.recalculateUserRoles(oldLead);
        }
        if (oldSubLead != null && (savedTeam.getSubLead() == null || !oldSubLead.getId().equals(savedTeam.getSubLead().getId()))) {
            userRoleRecalculationService.recalculateUserRoles(oldSubLead);
        }
        return savedTeam;
    }

    @Transactional
    public void deleteTeamById(Long id,String reason){
        Team existingTeam = getTeamById(id);

        Employee employee = securityUtils.getCurrentEmployee();
        if (!employee.getRoles().contains("ADMIN")){
            boolean isLead = existingTeam.getLead() != null && existingTeam.getLead().getId().equals(employee.getId());
            boolean isSubLead = existingTeam.getSubLead() != null && existingTeam.getSubLead().getId().equals(employee.getId());
            if (!isLead && !isSubLead){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Current User does not belong to this Team");
            }
        }

        teamEmployeeRepository.deleteByTeam(existingTeam);

        existingTeam.setIsDeleted(true);
        existingTeam.setDeletedAt(LocalDateTime.now());
        existingTeam.setDeletedBy(securityUtils.getCurrentUser().getId());
        existingTeam.setDeleteReason(reason);
        existingTeam.setStatus("INACTIVE");

        Team savedTeam = teamRepository.save(existingTeam);
        userRoleRecalculationService.recalculateUserRoles(savedTeam.getLead());
        userRoleRecalculationService.recalculateUserRoles(savedTeam.getSubLead());
    }

    @Transactional
    public void unassignSubLead(Long id) {
        Team team = getTeamById(id);

        Employee employee = securityUtils.getCurrentEmployee();
        if (!employee.getRoles().contains("ADMIN")){
            boolean isLead =
                    team.getLead() != null
                            && team.getLead().getId().equals(employee.getId());

            if (!isLead) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Current User is not allowed to unassign sublead"
                );
            }
        }

        Employee oldSubLead = team.getSubLead();

        if (oldSubLead != null) {
            team.setSubLead(null);
            teamRepository.save(team);
            userRoleRecalculationService.recalculateUserRoles(oldSubLead);
        }
    }
}
