package com.elite.employeemanager.auth.jwt.service;

import com.elite.employeemanager.auth.jwt.dto.AuthenticationResponse;
import com.elite.employeemanager.auth.jwt.dto.LoginRequest;
import com.elite.employeemanager.auth.jwt.dto.RefreshTokenRequest;
import com.elite.employeemanager.auth.refreshtoken.entity.RefreshToken;
import com.elite.employeemanager.auth.refreshtoken.service.RefreshTokenService;
import com.elite.employeemanager.auth.user.dto.UserDto;
import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.auth.user.repository.UserRepository;
import com.elite.employeemanager.auth.user.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;

    private List<String> getUserRoles(User user){
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(role -> role.replace("ROLE_", "")).toList();
    }

    public AuthenticationResponse login(LoginRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
        User user = (User) customUserDetailsService.loadUserByUsername(request.getEmail());
        String jwt = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return AuthenticationResponse.builder()
                .token(jwt)
                .refresh(refreshToken.getToken())
                .user(UserDto.builder()
                        .id(user.getId())
                        .email(user.getUsername())
                        .roles(getUserRoles(user)).build()
                )
                .build();
    }

    public AuthenticationResponse refresh(RefreshTokenRequest request) {
        return refreshTokenService.getByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> (User) customUserDetailsService.loadUserByUsername(user.getEmail()))
                .map(user -> {
                    String newAccessToken = jwtService.generateToken(user);
                    return new AuthenticationResponse(
                            newAccessToken,
                            request.getRefreshToken(),
                            UserDto.builder()
                                    .id(user.getId())
                                    .email(user.getUsername())
                                    .roles(getUserRoles(user)).build()
                    );
                }).orElseThrow(()->{
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid Refresh Token");
                });
    }
}
