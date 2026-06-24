package com.elite.employeemanager.feed.service;

import com.elite.employeemanager.feed.config.TeamsProperties;
import com.elite.employeemanager.feed.dto.TeamsTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TeamsTokenService {

    private final TeamsProperties teamsProperties;
    private final RestClient restClient;

    private String accessToken;
    private Instant expiresAt;

    public String getAccessToken() {
        if (accessToken != null && expiresAt != null && Instant.now().isBefore(expiresAt.minusSeconds(60))) {
            return accessToken;
        }
        return fetchNewToken();
    }

    private String fetchNewToken() {
        String tokenUrl =
                "https://login.microsoftonline.com/"
                        + teamsProperties.getTenantId()
                        + "/oauth2/v2.0/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", teamsProperties.getClientId());
        form.add("scope", "https://graph.microsoft.com/.default");
        form.add("client_secret", teamsProperties.getClientSecret());
        form.add("grant_type", "client_credentials");

        TeamsTokenResponse response =
                restClient.post()
                        .uri(tokenUrl)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form)
                        .retrieve()
                        .body(TeamsTokenResponse.class);
        if (response == null || response.getAccessToken() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Failed to obtain Teams access token");
        }
        accessToken = response.getAccessToken();
        this.expiresAt = Instant.now().plusSeconds(response.getExpiresIn());

        return accessToken;
    }

    public void invalidateToken() {
        accessToken = null;
        expiresAt = null;
    }
}
