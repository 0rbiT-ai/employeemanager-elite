package com.elite.employeemanager.auditsoftdelete.config;

import com.elite.employeemanager.auth.user.entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    @Bean
    public AuditorAware<Long> auditorProvider(){
        return ()->{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(auth==null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken){
                return Optional.empty();
            }
            User principal = (User) auth.getPrincipal();
            return Optional.of(principal.getId());
        };
    }
}
