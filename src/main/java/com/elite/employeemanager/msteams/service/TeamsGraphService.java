package com.elite.employeemanager.msteams.service;

import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.msteams.config.TeamsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamsGraphService {

    private final TeamsProperties teamsProperties;
    private final RestClient restClient;
    private final SecurityUtils securityUtils;

    private String cachedAccessToken;
    private Instant tokenExpiryTime;

    private synchronized String getAccessToken(){
        if (cachedAccessToken!=null && tokenExpiryTime!=null && Instant.now().isBefore(tokenExpiryTime)){
            return cachedAccessToken;
        }

        log.info("Requesting new oauth2 token from microsoft entra id...");
        String tokenUrl = "https://login.microsoftonline.com/" + teamsProperties.getTenantId() + "/oauth2/v2.0/token";
        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","client_credentials");
        body.add("client_id",teamsProperties.getClientId());
        body.add("client_secret",teamsProperties.getClientSecret());
        body.add("scope","https://graph.microsoft.com/.default");

        try {
            Map response = restClient.post()
                    .uri(java.net.URI.create(tokenUrl))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response==null|| !response.containsKey("access_token")){
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Failed to fetch access token from Microsoft");
            }
            this.cachedAccessToken = (String) response.get("access_token");

            Integer expiresInSeconds = (Integer) response.get("expires_in");
            this.tokenExpiryTime = Instant.now().plusSeconds(expiresInSeconds!=null? expiresInSeconds-30 : 3570);
            return this.cachedAccessToken;
        } catch (RestClientResponseException e){
            log.error("Microsoft OAuth token request failed. Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OAuth token retrieval failed", e);
        } catch (Exception e){
            log.error("Unexpected error obtaining Microsoft access token", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Teams Graph token generation failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> getMicrosoftTeams(){
        String token = getAccessToken();

        //String url = "https://graph.microsoft.com/v1.0/groups?$filter=resourceProvisioningOptions/any(x:x eq 'Team')&$select=id,displayName";

        URI uri = UriComponentsBuilder
                .fromUriString("https://graph.microsoft.com/v1.0/groups")
                .queryParam("$filter", "resourceProvisioningOptions/any(x:x eq 'Team')")
                .queryParam("$select", "id,displayName")
                .build()
                .toUri();

        try {
            Map response = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("value")) {
                return (List<Map<String, Object>>) response.get("value");
            }
            return Collections.emptyList();
        } catch (RestClientResponseException e){
            log.error("Failed to fetch teams from Graph API. Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode().value()), "Graph API call failed: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getChannelsForTeam(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teams Group ID is required");
        }

        String token = getAccessToken();

        //String url = "https://graph.microsoft.com/v1.0/teams/" + groupId + "/channels?$select=id,displayName";

        URI uri = UriComponentsBuilder
                .fromUriString("https://graph.microsoft.com/v1.0/teams/" + groupId + "/channels")
                .queryParam("$select", "id,displayName")
                .build()
                .toUri();

        try {
            Map response = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("value")) {
                return (List<Map<String, Object>>) response.get("value");
            }

            return Collections.emptyList();
        } catch (RestClientResponseException e) {
            log.error("Failed to fetch channels for team {}. Status: {}, Response: {}", groupId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode().value()), "Graph API call failed: " + e.getMessage(), e);
        }
    }
}
