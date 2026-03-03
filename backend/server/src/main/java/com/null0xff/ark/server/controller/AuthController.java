package com.null0xff.ark.server.controller;

import com.null0xff.ark.server.dto.AuthRequest;
import com.null0xff.ark.server.dto.AuthResponse;
import com.null0xff.ark.server.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.oauth2.discord.client-id}")
    private String clientId;

    @Value("${app.oauth2.discord.redirect-uri}")
    private String redirectUri;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@AuthenticationPrincipal Jwt jwt) {
        try {
            UUID userId = UUID.fromString(jwt.getSubject());
            String newToken = authService.refreshToken(userId);
            return ResponseEntity.ok(new AuthResponse(newToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/discord/url")
    public ResponseEntity<Map<String, String>> getDiscordAuthUrl() {
        String authUrl = "https://discord.com/api/oauth2/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=identify";
        return ResponseEntity.ok(Map.of("url", authUrl));
    }

    @PostMapping("/discord")
    public ResponseEntity<AuthResponse> loginWithDiscord(@RequestBody AuthRequest request) {
        try {
            String token = authService.loginWithDiscord(request.getCode());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
