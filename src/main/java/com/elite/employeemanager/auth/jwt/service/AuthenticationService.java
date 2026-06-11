package com.elite.employeemanager.auth.jwt.service;

import com.elite.employeemanager.auth.jwt.dto.AuthenticationResponse;
import com.elite.employeemanager.auth.jwt.dto.LoginRequest;
import com.elite.employeemanager.auth.jwt.dto.LoginResponse;
import com.elite.employeemanager.auth.jwt.dto.RefreshTokenRequest;
import com.elite.employeemanager.auth.mapping.entity.RoleComponent;
import com.elite.employeemanager.auth.mapping.entity.UserRole;
import com.elite.employeemanager.auth.mapping.repository.RoleComponentRepository;
import com.elite.employeemanager.auth.mapping.repository.UserRoleRepository;
import com.elite.employeemanager.auth.passwordreset.entity.PasswordResetToken;
import com.elite.employeemanager.auth.passwordreset.repository.PasswordResetTokenRepository;
import com.elite.employeemanager.auth.passwordreset.service.PasswordResetEmailService;
import com.elite.employeemanager.auth.refreshtoken.entity.RefreshToken;
import com.elite.employeemanager.auth.refreshtoken.service.RefreshTokenService;
import com.elite.employeemanager.auth.role.entity.Role;
import com.elite.employeemanager.auth.user.dto.UserDto;
import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.auth.user.repository.UserRepository;
import com.elite.employeemanager.auth.user.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRoleRepository userRoleRepository;
    private final RoleComponentRepository roleComponentRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetEmailService passwordResetEmailService;
    private final PasswordEncoder passwordEncoder;

    private List<String> getUserRoles(User user){
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(role -> role.replace("ROLE_", "")).toList();
    }

    private List<String> getUserPermissions(User user){
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth->!auth.startsWith("ROLE_")).toList();
    }

    private List<String> getUserComponents(User user){
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        if (userRoles.isEmpty()){
            return List.of();
        }

        List<Role> roles = userRoles.stream().map(UserRole::getRole).toList();
        return roleComponentRepository.findByRoleIn(roles).stream()
                .filter(RoleComponent::getCanView)
                .map(rc->rc.getComponent().getComponentKey())
                .distinct()
                .toList();
    }

    public LoginResponse login(LoginRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
        User user = (User) customUserDetailsService.loadUserByUsername(request.getEmail());
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        String jwt = jwtService.generateToken(user);

        ResponseCookie cookie = ResponseCookie.from("jwtToken",jwt)
                .httpOnly(true)
                .secure(false) // has to be true for production
                .path("/")
                .maxAge(60 * 60)
                .sameSite("Lax") // has to be None for production
                .build();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .refresh(refreshToken.getToken())
                .user(UserDto.builder()
                        .id(user.getId())
                        .email(user.getUsername())
                        .roles(getUserRoles(user))
                        .permissions(getUserPermissions(user))
                        .components(getUserComponents(user))
                        .build()
                )
                .build();

        return LoginResponse.builder()
                .authenticationResponse(authenticationResponse)
                .cookie(cookie)
                .build();
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        return refreshTokenService.getByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> (User) customUserDetailsService.loadUserByUsername(user.getEmail()))
                .map(user -> {
                    String newAccessToken = jwtService.generateToken(user);
                    ResponseCookie cookie = ResponseCookie.from("jwtToken",newAccessToken)
                            .httpOnly(true)
                            .secure(false)
                            .path("/")
                            .maxAge(60 * 60)
                            .sameSite("Lax")
                            .build();
                    AuthenticationResponse authenticationResponse = new AuthenticationResponse(
                            request.getRefreshToken(),
                            UserDto.builder()
                                    .id(user.getId())
                                    .email(user.getUsername())
                                    .roles(getUserRoles(user))
                                    .permissions(getUserPermissions(user))
                                    .components(getUserComponents(user))
                                    .build());
                    return LoginResponse.builder()
                            .authenticationResponse(authenticationResponse)
                            .cookie(cookie)
                            .build();

                }).orElseThrow(()->{
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid Refresh Token");
                });
    }

    public void forgotPassword(String email){
        userRepository.findByEmail(email).ifPresent(user->{
            passwordResetTokenRepository.deleteByUser(user);

            String token = UUID.randomUUID().toString();

            PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .build();

            passwordResetTokenRepository.save(passwordResetToken);

            passwordResetEmailService.sendPasswordResetEmail(user.getEmail(),token);
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword){
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid Password Reset Token"));

        if (Boolean.TRUE.equals(passwordResetToken.getIsUsed())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password Reset Token Already Used");
        }

        if (passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Password Reset Token Expired");
        }

        User user = passwordResetToken.getUser();

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        passwordResetToken.setIsUsed(true);
        passwordResetToken.setExpiresAt(LocalDateTime.now());
        userRepository.save(user);
        passwordResetTokenRepository.save(passwordResetToken);
    }
}
