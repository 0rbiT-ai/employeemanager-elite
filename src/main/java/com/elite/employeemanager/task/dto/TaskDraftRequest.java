package com.elite.employeemanager.task.dto;

import lombok.Data;

@Data
public class TaskDraftRequest {
    private String teamsMessage;
    private String teamsGroupId;
    private String teamsChannelId;
}
