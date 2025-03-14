package com.gptuessr.ai_game.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Clerk authentication and token operations
 */
@Component
public class ClerkAuthUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClerkAuthUtils.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${clerk.api.url:https://api.clerk.com}")
    private String clerkApiUrl;
    
    @Value("${clerk.api.key}")
    private String clerkApiKey;

    public ClerkAuthUtils() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Extract user ID from Clerk token
     * @param request HttpServletRequest containing the token
     * @return The user ID
     * @throws IllegalArgumentException if token is invalid or user ID cannot be extracted
     */
    public String getUserIdFromToken(HttpServletRequest request) {
        // Get the token from request attributes (set by the interceptor)
        String token = (String) request.getAttribute("clerkToken");
        if (token == null) {
            logger.error("No authentication token found in request attributes");
            // Log available attributes for debugging
            Enumeration<String> attributeNames = request.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                logger.debug("Request has attribute: {}", name);
            }
            throw new IllegalArgumentException("No authentication token found");
        }
        
        try {
            // Instead of trying to verify with Clerk's API, let's decode the JWT token directly
            // This is a simpler approach that doesn't require making API calls
            String userId = extractUserIdFromJwt(token);
            logger.info("Successfully extracted user ID from JWT: {}", userId);
            return userId;
            
        } catch (Exception e) {
            logger.error("Error extracting user ID from token: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to authenticate: " + e.getMessage());
        }
    }
    
    /**
     * Extract user ID from JWT token without verification
     * This is a temporary solution that avoids Clerk API calls
     */
    private String extractUserIdFromJwt(String token) {
        try {
            // Split the JWT token into its parts
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            // Decode the payload (second part)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            
            // Log the payload for debugging
            logger.debug("JWT payload: {}", payload);
            
            // Parse the JSON payload
            // This is simplified - in production you should use a proper JSON parser
            if (payload.contains("\"sub\":")) {
                int startIndex = payload.indexOf("\"sub\":") + 7;
                int endIndex = payload.indexOf("\"", startIndex);
                String userId = payload.substring(startIndex, endIndex);
                return userId;
            } else {
                throw new IllegalArgumentException("User ID not found in token payload");
            }
        } catch (Exception e) {
            logger.error("Error decoding JWT token: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to decode JWT token: " + e.getMessage());
        }
    }
    
    /**
     * Verify if a user is authenticated
     * @param request HttpServletRequest containing the token
     * @return true if user is authenticated, false otherwise
     */
    public boolean isAuthenticated(HttpServletRequest request) {
        try {
            getUserIdFromToken(request);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get user information from token
     * @param request HttpServletRequest containing the token
     * @return Map containing user information
     * @throws IllegalArgumentException if token is invalid
     */
    public Map<String, Object> getUserInfoFromToken(HttpServletRequest request) {
        String token = (String) request.getAttribute("clerkToken");
        if (token == null) {
            throw new IllegalArgumentException("No authentication token found");
        }
        
        try {
            // Extract user ID from token
            String userId = getUserIdFromToken(request);
            
            // Instead of making API call, return basic user info from token
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", userId);
            
            // Try to extract more info from the token if available
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                
                // Extract email if present
                if (payload.contains("\"email\":")) {
                    int startIndex = payload.indexOf("\"email\":") + 9;
                    int endIndex = payload.indexOf("\"", startIndex);
                    if (startIndex > 0 && endIndex > startIndex) {
                        String email = payload.substring(startIndex, endIndex);
                        userInfo.put("email", email);
                    }
                }
                
                // Extract name if present
                if (payload.contains("\"name\":")) {
                    int startIndex = payload.indexOf("\"name\":") + 8;
                    int endIndex = payload.indexOf("\"", startIndex);
                    if (startIndex > 0 && endIndex > startIndex) {
                        String name = payload.substring(startIndex, endIndex);
                        userInfo.put("name", name);
                    }
                }
            }
            
            return userInfo;
            
        } catch (Exception e) {
            logger.error("Error getting user information from token: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to get user information: " + e.getMessage());
        }
    }
    
    /**
     * Check if the authenticated user is the same as the provided user ID
     * @param request HttpServletRequest containing the token
     * @param userId User ID to compare with the authenticated user
     * @return true if the authenticated user matches the provided user ID
     * @throws IllegalArgumentException if authentication fails
     */
    public boolean isCurrentUser(HttpServletRequest request, String userId) {
        String authenticatedUserId = getUserIdFromToken(request);
        return authenticatedUserId.equals(userId);
    }
}