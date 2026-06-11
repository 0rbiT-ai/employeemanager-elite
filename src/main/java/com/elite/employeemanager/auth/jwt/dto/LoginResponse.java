package com.elite.employeemanager.auth.jwt.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseCookie;

@Data
@Builder
@Getter
@Setter
public class LoginResponse {
    private AuthenticationResponse authenticationResponse;
    private ResponseCookie cookie;
    private ResponseCookie refreshCookie;
}
