package com.elite.employeemanager.feed.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "teams")
@Data
public class TeamsProperties {

    private String tenantId;
    private String clientId;
    private String clientSecret;

    private String teamId;
    private String channelId;

}
