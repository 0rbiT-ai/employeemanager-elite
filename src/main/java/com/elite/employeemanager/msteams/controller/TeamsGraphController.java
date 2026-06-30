package com.elite.employeemanager.msteams.controller;

import com.elite.employeemanager.msteams.service.TeamsGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ms-teams")
@RequiredArgsConstructor
public class TeamsGraphController {

    private final TeamsGraphService teamsGraphService;

    @GetMapping("/groups")
    @PreAuthorize("hasAuthority('TEAM_CREATE')")
    public ResponseEntity<List<Map<String,Object>>> getMicrosoftTeams(){
        return new ResponseEntity<>(teamsGraphService.getMicrosoftTeams(), HttpStatus.OK);
    }

    @GetMapping("/groups/{groupId}/channels")
    @PreAuthorize("hasAuthority('TEAM_CREATE')")
    public ResponseEntity<List<Map<String,Object>>> getChannelsForTeam(@PathVariable String groupId){
        return new ResponseEntity<>(teamsGraphService.getChannelsForTeam(groupId), HttpStatus.OK);
    }

}
