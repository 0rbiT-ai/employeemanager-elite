package com.elite.employeemanager.msteams.service;
import java.util.Map;
import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.msteams.config.TeamsProperties;
import com.elite.employeemanager.msteams.dto.TeamsMessageRequest;
import com.elite.employeemanager.msteams.dto.TeamsMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamsService {

    private final TeamsProperties teamsProperties;
    private final RestClient restClient;
    private final SecurityUtils securityUtils;

    /**
     * Overloaded variant for background/scheduled jobs where no SecurityContext is available.
     * Accepts a sender name string directly instead of resolving it from the authenticated user.
     */
    public String postMessage(String title, String content, String senderName, String groupId, String channelId) {
        String formattedText = content + "<br/><br/><small><em>Posted by " + senderName + "</em></small>";
        Map<String, String> payload = Map.of(
                "title", title,
                "text", formattedText,
                "teamsGroupId", groupId != null ? groupId : "",
                "teamsChannelId", channelId != null ? channelId : ""
        );
        log.info("Sending message to Teams Webhook URL: {}", teamsProperties.getWebhookUrl());
        try {
            restClient.post()
                    .uri(java.net.URI.create(teamsProperties.getWebhookUrl()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            return "SUCCESS";
        } catch (RestClientResponseException e) {
            log.error("Teams webhook call failed. Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
            if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
                status = HttpStatus.BAD_GATEWAY;
            }
            throw new ResponseStatusException(status, "Teams webhook post failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling Teams webhook", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Teams webhook integration failed", e);
        }
    }
}
