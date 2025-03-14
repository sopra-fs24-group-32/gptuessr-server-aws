package com.gptuessr.ai_game.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller to handle Clerk token verification
 */
@RestController
@RequestMapping("/api/clerk")
public class ClerkApiController {

    private static final Logger logger = LoggerFactory.getLogger(ClerkApiController.class);
    
    @Value("${clerk.api.key}")
    private String clerkApiKey;
    
    @Value("${clerk.api.url:https://api.clerk.dev/v1}")
    private String clerkApiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Verify a JWT token from Clerk
     * 
     * @param token The JWT token to verify
     * @return ResponseEntity with verification result
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestParam String token) {
        try {
            // Prepare the verification endpoint URL
            String url = clerkApiUrl + "/tokens/verify";
            
            // Prepare headers and payload
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + clerkApiKey);
            headers.put("Content-Type", "application/json");
            
            Map<String, String> payload = new HashMap<>();
            payload.put("token", token);
            
            // Make the request to Clerk API
            ResponseEntity<Map> response = restTemplate.postForEntity(
                url, 
                payload, 
                Map.class,
                headers
            );
            
            // Return the verification result
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            }
            
        } catch (Exception e) {
            logger.error("Error verifying token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to verify token: " + e.getMessage()));
        }
    }
    
    /**
     * Get user information from a token
     * 
     * @param token The JWT token
     * @return ResponseEntity with user information
     */
    @GetMapping("/userInfo")
    public ResponseEntity<?> getUserInfo(@RequestParam String token) {
        try {
            // First verify the token
            ResponseEntity<?> verificationResponse = verifyToken(token);
            if (!verificationResponse.getStatusCode().is2xxSuccessful()) {
                return verificationResponse;
            }
            
            // Extract user ID from verified token
            @SuppressWarnings("unchecked")
            Map<String, Object> verificationData = (Map<String, Object>) verificationResponse.getBody();
            String userId = (String) ((Map<String, Object>) verificationData.get("payload")).get("sub");
            
            // Call Clerk API to get user details
            String url = clerkApiUrl + "/users/" + userId;
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + clerkApiKey);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(
                url,
                Map.class,
                headers
            );
            
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            logger.error("Error getting user info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get user info: " + e.getMessage()));
        }
    }
}
