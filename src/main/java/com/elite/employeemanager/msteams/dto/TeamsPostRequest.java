package com.elite.employeemanager.msteams.dto;

import lombok.Data;

@Data
public class TeamsPostRequest {
    private String title;
    private String message;
    private String teamsGroupId;
    private String teamsChannelId;
}
