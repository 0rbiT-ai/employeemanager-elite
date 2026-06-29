package com.elite.employeemanager.feed.dto;

import lombok.Data;

@Data
public class TeamsPostRequest {
    private String title;
    private String message;
    private String teamsGroupId;
    private String teamsChannelId;
}
