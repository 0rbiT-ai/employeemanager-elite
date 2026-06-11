package com.elite.employeemanager.auth.jwt.controller;

import com.elite.employeemanager.auth.jwt.dto.AuthenticationResponse;
import com.elite.employeemanager.auth.jwt.dto.LoginRequest;
import com.elite.employeemanager.auth.jwt.dto.LoginResponse;
import com.elite.employeemanager.auth.jwt.dto.RefreshTokenRequest;
import com.elite.employeemanager.auth.jwt.service.AuthenticationService;
import com.elite.employeemanager.auth.passwordreset.dto.ForgotPasswordRequest;
import com.elite.employeemanager.auth.passwordreset.dto.ResetPasswordRequest;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request){

        LoginResponse loginResponse = authenticationService.login(request);

        return ResponseEntity.ok()
                .header("Set-Cookie", loginResponse.getCookie().toString())
                .body(loginResponse.getAuthenticationResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request){

        LoginResponse loginResponse = authenticationService.refresh(request);

        return ResponseEntity.ok()
                .header("Set-Cookie",loginResponse.getCookie().toString())
                .body(loginResponse.getAuthenticationResponse());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        ResponseCookie cookie = ResponseCookie.from("jwtToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
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
