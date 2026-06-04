package com.elite.employeemanager.auth.jwt.dto;

import com.elite.employeemanager.auth.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private String token;
    private String refresh;
    private UserDto user;
}
