package com.elite.employeemanager.team.service;

import com.elite.employeemanager.auth.mapping.service.UserRoleRecalculationService;
import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.team.entity.Team;
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
        if (team.getStatus() == null) {
            team.setStatus("ACTIVE");
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

        userRoleRecalculationService.recalculateUserRoles(lead);
        if (subLead!=null){
            userRoleRecalculationService.recalculateUserRoles(subLead);
        }
        return teamRepository.save(team);
    }

    public List<Team> getAllTeams(){
        return teamRepository.findAll();
    }

    public Team getTeamById(Long id){
        return teamRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Team Not Found with Id : "+id));
    }

    @Transactional
    public Team updateTeamById(Long id, Team updatedTeam){

        Team existingTeam = getTeamById(id);
        Employee oldLead = existingTeam.getLead();
        Employee oldSubLead = existingTeam.getSubLead();

        if (updatedTeam.getTeamName()!=null){
            existingTeam.setTeamName(updatedTeam.getTeamName());
        }

        if (updatedTeam.getDescription()!=null){
            existingTeam.setDescription(updatedTeam.getDescription());
        }

        if (updatedTeam.getTeamsChannelId()!=null){
            existingTeam.setTeamsChannelId(updatedTeam.getTeamsChannelId());
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
        if (oldSubLead != null && !oldSubLead.getId().equals(savedTeam.getSubLead().getId())) {
            userRoleRecalculationService.recalculateUserRoles(oldSubLead);
        }
        return savedTeam;
    }

    @Transactional
    public void deleteTeamById(Long id,String reason){
        Team existingTeam = getTeamById(id);

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
        Employee oldSubLead = team.getSubLead();

        if (oldSubLead != null) {
            team.setSubLead(null);
            teamRepository.save(team);
            userRoleRecalculationService.recalculateUserRoles(oldSubLead);
        }
    }
}
