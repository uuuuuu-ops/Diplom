package com.diploma.Diplom.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }

        return (String) auth.getPrincipal(); 
    }


    public boolean hasRole(String role){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)return false;
        return auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(a -> a.equals("ROLE_" + role ));

        
    }
}