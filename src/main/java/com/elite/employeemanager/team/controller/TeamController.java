package com.elite.employeemanager.team.controller;

import com.elite.employeemanager.team.entity.Team;
import com.elite.employeemanager.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasAuthority('TEAM_CREATE')")
    public ResponseEntity<Team> addTeam(@RequestBody Team team){
        return new ResponseEntity<>(teamService.addTeam(team),HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TEAM_VIEW')")
    public ResponseEntity<List<Team>> getAllTeams(){
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TEAM_VIEW')")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id){
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TEAM_UPDATE')")
    public ResponseEntity<Team> updateTeamById(@PathVariable Long id, @RequestBody Team team){
        return new ResponseEntity<>(teamService.updateTeamById(id,team),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TEAM_DELETE')")
    public ResponseEntity<String> deleteTeamById(@PathVariable Long id, @RequestBody String reason){
        teamService.deleteTeamById(id, reason);
        return ResponseEntity.ok("Team Deleted");
    }

    @PatchMapping("/{id}/unassign-sublead")
    @PreAuthorize("hasAuthority('TEAM_UPDATE')")
    public ResponseEntity<String> unassignSubLead(@PathVariable Long id) {
        teamService.unassignSubLead(id);
        return ResponseEntity.ok("Sub-lead unassigned successfully");
    }
}
