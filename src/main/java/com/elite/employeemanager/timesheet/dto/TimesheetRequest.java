package com.elite.employeemanager.timesheet.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TimesheetRequest {
    private Long employeeId;
    private Long taskId;
    private Long projectId;
    private String bugNumber;
    private String workCategory;
    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal durationHours;
    private String description;
    private String justification;
}
