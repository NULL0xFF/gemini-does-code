package com.null0xff.ark.server.service;

import com.null0xff.ark.server.entity.User;
import com.null0xff.ark.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class AuthService {

    @Value("${app.oauth2.discord.client-id}")
    private String clientId;

    @Value("${app.oauth2.discord.client-secret}")
    private String clientSecret;

    @Value("${app.oauth2.discord.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;

    public AuthService(RestTemplate restTemplate, UserRepository userRepository, JwtTokenService jwtTokenService) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
    }

    public String loginWithDiscord(String code) {
        String discordAccessToken = fetchDiscordAccessToken(code);
        Map<String, Object> discordProfile = fetchDiscordUserProfile(discordAccessToken);
        User user = syncUserWithDatabase(discordProfile);
        String token = jwtTokenService.generateToken(user);
        user.setCurrentToken(token);
        userRepository.save(user);
        return token;
    }

    public String refreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found during token refresh"));
        user.setLastLogin(LocalDateTime.now());
        String newToken = jwtTokenService.generateToken(user);
        user.setCurrentToken(newToken);
        userRepository.save(user);
        return newToken;
    }

    private String fetchDiscordAccessToken(String code) {
        String tokenUrl = "https://discord.com/api/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
        
        if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
            throw new RuntimeException("Failed to retrieve Discord access token");
        }
        return (String) response.getBody().get("access_token");
    }

    private Map<String, Object> fetchDiscordUserProfile(String accessToken) {
        String profileUrl = "https://discord.com/api/users/@me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(profileUrl, HttpMethod.GET, request, Map.class);
        
        if (response.getBody() == null) {
            throw new RuntimeException("Failed to retrieve Discord user profile");
        }
        return response.getBody();
    }

    private User syncUserWithDatabase(Map<String, Object> discordProfile) {
        String discordId = (String) discordProfile.get("id");
        String username = (String) discordProfile.get("username");
        String avatar = (String) discordProfile.get("avatar");

        User user = userRepository.findByDiscordId(discordId).orElse(new User());
        user.setDiscordId(discordId);
        user.setUsername(username);
        user.setAvatar(avatar);
        user.setLastLogin(LocalDateTime.now());
        
        return userRepository.save(user);
    }
}
