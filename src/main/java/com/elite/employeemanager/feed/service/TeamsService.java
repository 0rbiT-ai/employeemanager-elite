package com.elite.employeemanager.feed.service;

import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.feed.config.TeamsProperties;
import com.elite.employeemanager.feed.dto.TeamsMessageRequest;
import com.elite.employeemanager.feed.dto.TeamsMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamsService {

    private final TeamsTokenService teamsTokenService;
    private final TeamsProperties teamsProperties;
    private final RestClient restClient;
    private final SecurityUtils securityUtils;

    public String postMessage(String title, String content) {
        return postMessageInternal(title, content, true);
    }

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
}
