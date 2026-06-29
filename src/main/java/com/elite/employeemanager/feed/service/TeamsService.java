package com.elite.employeemanager.feed.service;
import java.util.Map;
import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.feed.config.TeamsProperties;
import com.elite.employeemanager.feed.dto.TeamsMessageRequest;
import com.elite.employeemanager.feed.dto.TeamsMessageResponse;
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

    /*
    public String postMessage(String title, String content) {
        Employee currentEmployee = securityUtils.getCurrentEmployee();
        return postMessage(title, content, currentEmployee.getName());
    }
    */

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
    /*
    private String postMessageInternal(String title, String content, boolean retryOnUnauthorized) {
        String token = teamsTokenService.getAccessToken();

        Employee currentEmployee = securityUtils.getCurrentEmployee();

        String html = "<h3>" + title + "</h3><p>" + content + "</p><br/><small><em>Posted by "+currentEmployee.getName()+"</em></small>";

        TeamsMessageRequest request = new TeamsMessageRequest(new TeamsMessageRequest.Body(html));

        try {
            TeamsMessageResponse response = restClient.post()
                    .uri("https://graph.microsoft.com/v1.0/teams/"
                            + teamsProperties.getTeamId()
                            + "/channels/"
                            + teamsProperties.getChannelId()
                            + "/messages")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(request)
                    .retrieve()
                    .body(TeamsMessageResponse.class);

            if (response == null || response.getId() == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Teams message post returned empty response");
            }
            return response.getId();

        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Teams Graph API call returned 401 Unauthorized. Cached token might be expired/invalid.");
            if (retryOnUnauthorized) {
                log.info("Invalidating cached token and retrying with a fresh token...");
                teamsTokenService.invalidateToken();
                return postMessageInternal(title, content, false);
            } else {
                log.error("Teams Graph API call returned 401 Unauthorized again on retry. Aborting.");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Teams authentication failed. Please verify credentials.", e);
            }
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("Teams Graph API call returned 403 Forbidden. This indicates permission/policy issues in Teams/Azure AD. Graph Error details: {}", e.getResponseBodyAsString(), e);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Teams integration not configured or lacks permissions (403)", e);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 429) {
                log.error("Teams Graph API call returned 429 Too Many Requests. Rate limit exceeded. Response: {}", e.getResponseBodyAsString(), e);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Teams integration rate limit exceeded (429)", e);
            }
            log.error("Teams Graph API call failed with status: {}, response: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode().value()), "Teams integration failed", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred while posting to Teams", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Teams integration failed due to an unexpected error", e);
        }
    }
     */
}
