package com.elite.employeemanager.auth.user.service;

import com.elite.employeemanager.auth.mapping.entity.RolePermission;
import com.elite.employeemanager.auth.mapping.entity.UserRole;
import com.elite.employeemanager.auth.mapping.repository.RolePermissionRepository;
import com.elite.employeemanager.auth.mapping.repository.UserRoleRepository;
import com.elite.employeemanager.auth.role.entity.Role;
import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.auth.user.repository.UserRepository;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not found with email: "+ email));

        List<GrantedAuthority> authorities = new ArrayList<>();
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        for (UserRole userRole: userRoles){
            String roleAuthority = "ROLE_"+userRole.getRole().getRoleCode();

            authorities.add(new SimpleGrantedAuthority(roleAuthority));

            List<RolePermission> rolePermissions = rolePermissionRepository.findByRole(userRole.getRole());
            for (RolePermission rp:rolePermissions){
                authorities.add(new SimpleGrantedAuthority(rp.getPermission().getPermissionName()));
            }
        }

        user.setAuthorities(authorities);
        return user;
    }
}
