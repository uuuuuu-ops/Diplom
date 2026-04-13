package com.diploma.Diplom.util;

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
}