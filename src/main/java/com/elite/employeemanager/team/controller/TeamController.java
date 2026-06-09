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
@PreAuthorize("hasAuthority('TEAM_MANAGE')")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<Team> addTeam(@RequestBody Team team){
        return new ResponseEntity<>(teamService.addTeam(team),HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams(){
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id){
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Team> updateTeamById(@PathVariable Long id, @RequestBody Team team){
        return new ResponseEntity<>(teamService.updateTeamById(id,team),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTeamById(@PathVariable Long id, @RequestBody String reason){
        teamService.deleteTeamById(id, reason);
        return ResponseEntity.ok("Team Deleted");
    }
}
