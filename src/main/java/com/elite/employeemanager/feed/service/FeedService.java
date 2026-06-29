package com.elite.employeemanager.feed.service;

import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.feed.entity.Feed;
import com.elite.employeemanager.feed.repository.FeedRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {
    private final FeedRepository feedRepository;
    private final SecurityUtils securityUtils;
    private final TeamsService teamsService;

    @Transactional
    public Feed createAnnouncement(Feed announcement){

        boolean publishInternal = Boolean.TRUE.equals(announcement.getPublishToInternal());
        boolean publishTeams = Boolean.TRUE.equals(announcement.getPublishToTeams());

        if (!publishTeams && !publishInternal) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at least one destination");
        }
        if (!List.of("INFO","WARNING","CRITICAL","SUCCESS")
                .contains(announcement.getSeverity())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid severity"
            );
        }
        if (announcement.getTitle()==null||announcement.getTitle().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Announcement title required");
        }
        if (announcement.getContent()==null||announcement.getContent().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Announcement content required");
        }

        Employee currentEmployee = securityUtils.getCurrentEmployee();

        Feed feed = Feed.builder()
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .severity(announcement.getSeverity())
                .publishToInternal(publishInternal)
                .publishToTeams(publishTeams)
                .createdBy(currentEmployee)
                .createdAt(LocalDateTime.now())
                .build();

        if (publishTeams) {
            feed.setTeamsPostStatus("PENDING");
        }

        feed = feedRepository.save(feed);

        /*
        if (publishTeams) {
            try {
                String messageId = teamsService.postMessage(feed.getTitle(), feed.getContent());
                feed.setTeamsPostStatus("SUCCESS");
                feed.setTeamsMessageId(messageId);
            } catch (Exception e) {
                feed.setTeamsPostStatus("FAILED");
                log.error("Failed to post feed {} to Teams", feed.getId(), e);
            }
            feedRepository.save(feed);
        }
        */
        return feed;
    }

    public List<Feed> getAllAnnouncements() {
        return feedRepository.findAll();
    }

    public Feed getAnnouncementById(Long id) {
        return feedRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Announcement not found"));
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        Feed feed = getAnnouncementById(id);
        feedRepository.delete(feed);
    }
}
