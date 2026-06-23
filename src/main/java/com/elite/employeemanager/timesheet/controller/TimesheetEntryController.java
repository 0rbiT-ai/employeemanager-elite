package com.elite.employeemanager.timesheet.controller;

import com.elite.employeemanager.timesheet.dto.TimesheetRequest;
import com.elite.employeemanager.timesheet.dto.TimesheetResponse;
import com.elite.employeemanager.timesheet.dto.TimesheetStatusUpdateRequest;
import com.elite.employeemanager.timesheet.entity.TimesheetEntry;
import com.elite.employeemanager.timesheet.service.TimesheetEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/timesheets")
@RequiredArgsConstructor
public class TimesheetEntryController {

    private final TimesheetEntryService timesheetService;

    @GetMapping
    public ResponseEntity<List<TimesheetResponse>> getAllTimesheetEntries(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(timesheetService.getAllEntries(employeeId, date, status));
    }

    @PostMapping
    public ResponseEntity<TimesheetResponse> createTimesheetEntry(@RequestBody TimesheetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timesheetService.createEntry(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TimesheetResponse> updateEntryStatus(@PathVariable Long id, @RequestBody TimesheetStatusUpdateRequest request) {
        return ResponseEntity.ok(timesheetService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTimesheetEntry(@PathVariable Long id) {
        timesheetService.deleteEntry(id);
        return ResponseEntity.ok("Timesheet Entry deleted successfully");
    }
}
