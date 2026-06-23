package com.elite.employeemanager.timesheet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TimesheetResponse {
    private Long id;
    private NestedId employee;
    private NestedId task;
    private NestedId project;
    private String bugNumber;
    private String workCategory;
    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal durationHours;
    private String description;
    private String justification;
    private String status;
    private String managerComment;
    private LocalDateTime approvedAt;
    private NestedId approvedBy;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NestedId{
        private Long id;
    }
}
