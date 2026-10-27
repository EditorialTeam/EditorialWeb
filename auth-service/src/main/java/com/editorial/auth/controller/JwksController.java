package com.editorial.auth.controller;

import com.editorial.auth.service.JwtService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwksController {
    private final JwtService service;
    public JwksController(JwtService service) {
        this.service = service;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwks() {
        return service.getPublicKeys();
    }
}
