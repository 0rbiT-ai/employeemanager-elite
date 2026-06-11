package com.elite.employeemanager.auth.jwt.controller;

import com.elite.employeemanager.auth.jwt.dto.AuthenticationResponse;
import com.elite.employeemanager.auth.jwt.dto.LoginRequest;
import com.elite.employeemanager.auth.jwt.dto.LoginResponse;

import com.elite.employeemanager.auth.jwt.service.AuthenticationService;
import com.elite.employeemanager.auth.passwordreset.dto.ForgotPasswordRequest;
import com.elite.employeemanager.auth.passwordreset.dto.ResetPasswordRequest;

import com.elite.employeemanager.auth.refreshtoken.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request){

        LoginResponse loginResponse = authenticationService.login(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", loginResponse.getCookie().toString());
        headers.add("Set-Cookie", loginResponse.getRefreshCookie().toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(loginResponse.getAuthenticationResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(HttpServletRequest request){

        LoginResponse loginResponse = authenticationService.refresh(request);

        return ResponseEntity.ok()
                .header("Set-Cookie",loginResponse.getCookie().toString())
                .body(loginResponse.getAuthenticationResponse());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {

        String refreshToken = null;

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken!=null){
            refreshTokenService.deleteByToken(refreshToken);
        }

        ResponseCookie cookie = ResponseCookie.from("jwtToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", cookie.toString());
        headers.add("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body("Logged out");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request){
        authenticationService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("If the email exists, a password reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request){
        authenticationService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password Reset Successfully");
    }
}
