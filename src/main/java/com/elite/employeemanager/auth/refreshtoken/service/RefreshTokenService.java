package com.elite.employeemanager.auth.refreshtoken.service;

import com.elite.employeemanager.auth.refreshtoken.entity.RefreshToken;
import com.elite.employeemanager.auth.refreshtoken.repository.RefreshTokenRepository;
import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.auth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshToken createRefreshToken(Long id){
        Optional<RefreshToken> existingRefreshToken = refreshTokenRepository.findByUserId(id);
        if(existingRefreshToken.isPresent()){
            RefreshToken refreshToken = existingRefreshToken.get();
            refreshToken.setExpiresAt(LocalDateTime.now().plusHours(3));
            refreshToken.setToken(UUID.randomUUID().toString());
            return refreshTokenRepository.save(refreshToken);
        }
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findById(id).orElseThrow());
        refreshToken.setExpiresAt(LocalDateTime.now().plusHours(3));
        refreshToken.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.isRevoked()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Refresh token revoked. Login again");
        }

        if(token.getExpiresAt().isBefore(LocalDateTime.now())){
            refreshTokenRepository.delete(token);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Refresh token expired. Login again");
        }
        return token;
    }

    public Optional<RefreshToken> getByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteByToken(String token){
        refreshTokenRepository.deleteByToken(token);
    }
    @Transactional
    public void deleteByUser(User user){
        refreshTokenRepository.deleteByUser(user);
    }
}
