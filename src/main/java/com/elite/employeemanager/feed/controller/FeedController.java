package com.elite.employeemanager.feed.controller;

import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.feed.dto.TeamsPostRequest;
import com.elite.employeemanager.feed.entity.Feed;
import com.elite.employeemanager.feed.service.FeedService;
import com.elite.employeemanager.feed.service.TeamsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final TeamsService teamsService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_VIEW')")
    public ResponseEntity<List<Feed>> getAllAnnouncements(){
        return new ResponseEntity<>(feedService.getAllAnnouncements(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_VIEW')")
    public ResponseEntity<Feed> getAnnouncementById(@PathVariable Long id){
        return new ResponseEntity<>(feedService.getAnnouncementById(id), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_CREATE')")
    public ResponseEntity<Feed> createAnnouncement(@RequestBody Feed announcement){
        return new ResponseEntity<>(feedService.createAnnouncement(announcement), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_DELETE')")
    public ResponseEntity<String> deleteAnnouncementById(@PathVariable Long id){
        feedService.deleteAnnouncement(id);
        return new ResponseEntity<>("Announcement deleted successfully", HttpStatus.OK);
    }

    @PostMapping("/teams-post")
    @PreAuthorize("hasAuthority('TEAMS_POST')")
    public ResponseEntity<String> postToTeams(@RequestBody TeamsPostRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content is required");
        }
        if (request.getTeamsGroupId() == null || request.getTeamsGroupId().isBlank() ||
                request.getTeamsChannelId() == null || request.getTeamsChannelId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teams Group ID and Channel ID are required");
        }
        String title = request.getTitle() != null && !request.getTitle().isBlank() 
                ? request.getTitle() 
                : "App Notification";
                
        teamsService.postMessage(title, request.getMessage(),securityUtils.getCurrentEmployee().getName(),request.getTeamsGroupId(), request.getTeamsChannelId());
        return new ResponseEntity<>("Message posted to Teams successfully", HttpStatus.OK);
    }
}
