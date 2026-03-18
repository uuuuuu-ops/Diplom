package com.diploma.Diplom.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    @GetMapping("/debug/me")
    public Object me(Authentication authentication) {
        return authentication;
    }
}