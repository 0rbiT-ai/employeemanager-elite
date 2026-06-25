package com.elite.employeemanager.meeting.controller;

import com.elite.employeemanager.attachment.entity.Attachment;
import com.elite.employeemanager.attachment.service.AttachmentService;
import com.elite.employeemanager.meeting.entity.Meeting;
import com.elite.employeemanager.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    // 1. Create a new meeting
    @PostMapping
    @PreAuthorize("hasAuthority('MEETING_CREATE')")
    public ResponseEntity<Meeting> createMeeting(@RequestBody Meeting meeting) {
        return new ResponseEntity<>(meetingService.createMeeting(meeting), HttpStatus.CREATED);
    }

    // 2. View all meetings (All Meetings Tab)
    @GetMapping
    @PreAuthorize("hasAuthority('MEETING_VIEW')")
    public ResponseEntity<List<Meeting>> getAllMeetings() {
        return new ResponseEntity<>(meetingService.getAllMeetings(), HttpStatus.OK);
    }

    // 3. View personal meetings (Personal Meetings Tab)
    @GetMapping("/personal")
    @PreAuthorize("hasAuthority('MEETING_VIEW')")
    public ResponseEntity<List<Meeting>> getPersonalMeetings() {
        return new ResponseEntity<>(meetingService.getPersonalMeetings(), HttpStatus.OK);
    }

    // 4. Update meeting details (checks creator or management inside service)
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MEETING_UPDATE')")
    public ResponseEntity<Meeting> updateMeeting(@PathVariable Long id, @RequestBody Meeting meeting) {
        return new ResponseEntity<>(meetingService.updateMeeting(id, meeting), HttpStatus.OK);
    }

    // 5. Delete/cancel a meeting (checks creator or management inside service)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MEETING_DELETE')")
    public ResponseEntity<String> deleteMeeting(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return new ResponseEntity<>("Meeting deleted successfully", HttpStatus.OK);
    }

    // 6. Add attendee to a meeting
    @PostMapping("/{id}/attendees/{employeeId}")
    @PreAuthorize("hasAuthority('MEETING_UPDATE')")
    public ResponseEntity<Meeting> addAttendee(@PathVariable Long id, @PathVariable Long employeeId) {
        return new ResponseEntity<>(meetingService.addAttendee(id, employeeId), HttpStatus.OK);
    }

    // 7. Remove attendee from a meeting
    @DeleteMapping("/{id}/attendees/{employeeId}")
    @PreAuthorize("hasAuthority('MEETING_UPDATE')")
    public ResponseEntity<Meeting> removeAttendee(@PathVariable Long id, @PathVariable Long employeeId) {
        return new ResponseEntity<>(meetingService.removeAttendee(id, employeeId), HttpStatus.OK);
    }

}
