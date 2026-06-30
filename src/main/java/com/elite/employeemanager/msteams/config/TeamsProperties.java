package com.elite.employeemanager.msteams.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "teams")
@Data
public class TeamsProperties {

    private String webhookUrl;
}