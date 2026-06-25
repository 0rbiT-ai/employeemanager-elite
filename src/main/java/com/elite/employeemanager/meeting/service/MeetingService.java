package com.elite.employeemanager.meeting.service;

import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.meeting.entity.Meeting;
import com.elite.employeemanager.meeting.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final SecurityUtils securityUtils;
    private final EmployeeRepository employeeRepository;

    // 1. Create a meeting (any authenticated employee can schedule)
    public Meeting createMeeting(Meeting meeting) {
        if (meeting.getTitle() == null || meeting.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Meeting title is required");
        }
        if (meeting.getMeetingLink() == null || meeting.getMeetingLink().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Meeting link is required");
        }
        if (meeting.getStartTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date and time is required");
        }
        if (meeting.getDurationMinutes() == null || meeting.getDurationMinutes() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid duration is required");
        }
        // Save will automatically assign createdBy / createdAt via JPA Auditing
        return meetingRepository.save(meeting);
    }

    // 2. Read: Get all meetings (for the "All Meetings" tab)
    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }

    // 3. Read: Get personal meetings (for the "Personal Meetings" tab)
    public List<Meeting> getPersonalMeetings() {
        Employee currentEmployee = securityUtils.getCurrentEmployee();
        return meetingRepository.findPersonalMeetings(currentEmployee.getId(), currentEmployee.getUser().getId());
    }

    // 4. Update: Only management roles or the meeting creator can update
    public Meeting updateMeeting(Long id, Meeting details) {

        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting not found"));

        checkOwnershipOrPrivilege(meeting, "update");

        meeting.setTitle(details.getTitle());
        meeting.setDescription(details.getDescription());
        meeting.setMeetingLink(details.getMeetingLink());
        meeting.setStartTime(details.getStartTime());
        meeting.setDurationMinutes(details.getDurationMinutes());
        meeting.setProject(details.getProject());
        meeting.setTask(details.getTask());
        meeting.setAttendees(details.getAttendees());
        return meetingRepository.save(meeting);
    }

    // 5. Delete: Only management roles or the meeting creator can delete
    public void deleteMeeting(Long id) {

        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting not found"));

        checkOwnershipOrPrivilege(meeting, "delete");

        meetingRepository.delete(meeting);
    }

    // Add attendee
    public Meeting addAttendee(Long id, Long employeeId) {

        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting not found"));

        checkOwnershipOrPrivilege(meeting, "modify attendees for");

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        meeting.getAttendees().add(employee);
        return meetingRepository.save(meeting);
    }

    // Remove attendee
    public Meeting removeAttendee(Long id, Long employeeId) {

        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting not found"));

        checkOwnershipOrPrivilege(meeting, "modify attendees for");

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        meeting.getAttendees().remove(employee);
        return meetingRepository.save(meeting);
    }


    // Helper: Validates if the user is authorized to modify the meeting
    private void checkOwnershipOrPrivilege(Meeting meeting, String action) {
        Employee currentEmployee = securityUtils.getCurrentEmployee();

        // Admins, Team Leads, and Sub Leads can modify any meeting
        boolean isManagement = currentEmployee.getRoles().contains("ADMIN")
                || currentEmployee.getRoles().contains("TEAM_LEAD")
                || currentEmployee.getRoles().contains("SUB_LEAD");

        // The creator is matched against the User ID stored by JPA Auditing (createdBy)
        boolean isCreator = Objects.equals(meeting.getCreatedBy(), currentEmployee.getUser().getId());
        if (!isManagement && !isCreator) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to " + action + " this meeting");
        }
    }
}
