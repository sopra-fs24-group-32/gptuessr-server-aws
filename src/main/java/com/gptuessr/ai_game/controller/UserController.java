package com.gptuessr.ai_game.controller;

import com.gptuessr.ai_game.entity.User;
import com.gptuessr.ai_game.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS
}, allowCredentials = "true")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @Value("${clerk.webhook.secret}")
    private String clerkWebhookSecret;
    
    /**
     * Handle Clerk webhook events
     * @param svix_id The Svix ID header for verification
     * @param svix_timestamp The Svix timestamp header for verification
     * @param svix_signature The Svix signature header for verification
     * @param payload The webhook payload
     * @return ResponseEntity with status
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleClerkWebhook(
            @RequestHeader("svix-id") String svix_id,
            @RequestHeader("svix-timestamp") String svix_timestamp,
            @RequestHeader("svix-signature") String svix_signature,
            @RequestBody String payload) {
        
        logger.info("Received Clerk webhook: {}", svix_id);
        
        // Verify webhook signature
        if (!verifyWebhookSignature(svix_id, svix_timestamp, svix_signature, payload)) {
            logger.warn("Invalid webhook signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }
        
        try {
            // Parse event type from payload
            String eventType = extractEventType(payload);
            String userId = extractUserId(payload);
            
            if (userId == null) {
                logger.warn("No user ID found in payload");
                return ResponseEntity.ok().build();
            }
            
            logger.info("Processing webhook event: {} for user: {}", eventType, userId);
            
            switch (eventType) {
                case "user.created":
                    handleUserCreated(payload);
                    break;
                case "user.updated":
                    handleUserUpdated(payload);
                    break;
                case "user.deleted":
                    handleUserDeleted(payload);
                    break;
                case "session.created":
                    handleSessionCreated(payload);
                    break;
                case "session.ended":
                    handleSessionEnded(payload);
                    break;
                default:
                    logger.info("Unhandled webhook event: {}", eventType);
            }
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }
    
    /**
     * API endpoint to manually register or update a user from Clerk
     * @param userData Map containing user data
     * @return ResponseEntity with user data
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> userData) {
        try {
            String clerkUserId = (String) userData.get("clerkUserId");
            String username = (String) userData.get("username");
            String email = (String) userData.get("email");
            
            if (clerkUserId == null || username == null || email == null) {
                return ResponseEntity.badRequest().body("Missing required fields: clerkUserId, username, and email are required");
            }
            
            // Extract optional data
            String firstName = (String) userData.get("firstName");
            String lastName = (String) userData.get("lastName");
            String profilePicture = (String) userData.get("profilePicture");
            
            // Get provider info if available
            @SuppressWarnings("unchecked")
            Map<String, String> providerInfo = (Map<String, String>) userData.get("providerInfo");
            
            // Check if user already exists
            Optional<User> existingUser = userService.findByClerkUserId(clerkUserId);
            if (existingUser.isPresent()) {
                // Update existing user
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("username", username);
                updateData.put("email", email);
                if (firstName != null) updateData.put("firstName", firstName);
                if (lastName != null) updateData.put("lastName", lastName);
                if (profilePicture != null) updateData.put("profilePicture", profilePicture);
                
                User updatedUser = userService.updateUserInfo(clerkUserId, updateData);
                return ResponseEntity.ok(updatedUser);
            } else {
                // Register new user
                User newUser = userService.registerClerkUser(clerkUserId, username, email, 
                    firstName, lastName, profilePicture, providerInfo);
                return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
            }
            
        } catch (Exception e) {
            logger.error("Error registering user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error registering user: " + e.getMessage());
        }
    }
    
    /**
     * API endpoint to update user login status
     * @param loginData Map containing login data
     * @return ResponseEntity with updated user data
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        try {
            String clerkUserId = loginData.get("clerkUserId");
            String clerkSessionId = loginData.get("clerkSessionId");
            
            if (clerkUserId == null) {
                return ResponseEntity.badRequest().body("Missing required field: clerkUserId");
            }
            
            User updatedUser = userService.updateUserOnLogin(clerkUserId, clerkSessionId);
            if (updatedUser != null) {
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            
        } catch (Exception e) {
            logger.error("Error processing user login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing login: " + e.getMessage());
        }
    }
    
    /**
     * API endpoint to update user logout status
     * @param logoutData Map containing logout data
     * @return ResponseEntity with status
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody Map<String, String> logoutData) {
        try {
            String clerkUserId = logoutData.get("clerkUserId");
            
            if (clerkUserId == null) {
                return ResponseEntity.badRequest().body("Missing required field: clerkUserId");
            }
            
            User updatedUser = userService.updateUserLogout(clerkUserId);
            if (updatedUser != null) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            
        } catch (Exception e) {
            logger.error("Error processing user logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing logout: " + e.getMessage());
        }
    }
    
    /**
     * Get user information by Clerk user ID
     * @param clerkUserId The unique user ID from Clerk
     * @return ResponseEntity with user data
     */
    @GetMapping("/{clerkUserId}")
    public ResponseEntity<?> getUserByClerkId(@PathVariable String clerkUserId) {
        try {
            Optional<User> user = userService.findByClerkUserId(clerkUserId);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            logger.error("Error fetching user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching user: " + e.getMessage());
        }
    }
    
    /**
     * Handle user.created webhook event
     * @param payload The webhook payload
     */
    private void handleUserCreated(String payload) {
        try {
            // Extract user data from payload
            String clerkUserId = extractUserId(payload);
            String username = extractUsername(payload);
            String email = extractEmail(payload);
            String firstName = extractFirstName(payload);
            String lastName = extractLastName(payload);
            String profilePicture = extractProfilePicture(payload);
            Map<String, String> providerInfo = extractProviderInfo(payload);
            
            if (clerkUserId != null && username != null && email != null) {
                userService.registerClerkUser(clerkUserId, username, email, firstName, lastName, profilePicture, providerInfo);
            }
        } catch (Exception e) {
            logger.error("Error handling user.created webhook", e);
        }
    }
    
    /**
     * Handle user.updated webhook event
     * @param payload The webhook payload
     */
    private void handleUserUpdated(String payload) {
        try {
            String clerkUserId = extractUserId(payload);
            
            if (clerkUserId != null) {
                Map<String, Object> userData = new HashMap<>();
                
                String username = extractUsername(payload);
                if (username != null) userData.put("username", username);
                
                String email = extractEmail(payload);
                if (email != null) userData.put("email", email);
                
                String firstName = extractFirstName(payload);
                if (firstName != null) userData.put("firstName", firstName);
                
                String lastName = extractLastName(payload);
                if (lastName != null) userData.put("lastName", lastName);
                
                String profilePicture = extractProfilePicture(payload);
                if (profilePicture != null) userData.put("profilePicture", profilePicture);
                
                userService.updateUserInfo(clerkUserId, userData);
            }
        } catch (Exception e) {
            logger.error("Error handling user.updated webhook", e);
        }
    }
    
    /**
     * Handle user.deleted webhook event
     * @param payload The webhook payload
     */
    private void handleUserDeleted(String payload) {
        // Implement user deletion logic if needed
        // For example, you might want to anonymize user data rather than delete it
        logger.info("User deleted webhook received. Implement deletion logic if needed.");
    }
    
    /**
     * Handle session.created webhook event
     * @param payload The webhook payload
     */
    private void handleSessionCreated(String payload) {
        try {
            String clerkUserId = extractUserId(payload);
            String sessionId = extractSessionId(payload);
            
            if (clerkUserId != null && sessionId != null) {
                userService.updateUserOnLogin(clerkUserId, sessionId);
            }
        } catch (Exception e) {
            logger.error("Error handling session.created webhook", e);
        }
    }
    
    /**
     * Handle session.ended webhook event
     * @param payload The webhook payload
     */
    private void handleSessionEnded(String payload) {
        try {
            String clerkUserId = extractUserId(payload);
            
            if (clerkUserId != null) {
                userService.updateUserLogout(clerkUserId);
            }
        } catch (Exception e) {
            logger.error("Error handling session.ended webhook", e);
        }
    }
    
    /**
     * Verify the webhook signature
     * @param svix_id The Svix ID header
     * @param svix_timestamp The Svix timestamp header
     * @param svix_signature The Svix signature header
     * @param payload The webhook payload
     * @return true if signature is valid
     */
    private boolean verifyWebhookSignature(String svix_id, String svix_timestamp, String svix_signature, String payload) {
        try {
            // Prepare the message for signing
            String message = svix_id + "." + svix_timestamp + "." + payload;
            
            // Create HMAC SHA-256 signer
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(clerkWebhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKey);
            
            // Calculate signature
            byte[] hash = hmacSha256.doFinal(message.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = "v1," + Base64.getEncoder().encodeToString(hash);
            
            // Split the received signatures
            String[] signatures = svix_signature.split(" ");
            
            // Check if any of the received signatures match our calculated one
            for (String signature : signatures) {
                if (signature.equals(calculatedSignature)) {
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error verifying webhook signature", e);
            return false;
        }
    }
    
    /**
     * Extract event type from payload
     * @param payload The webhook payload
     * @return The event type string
     */
    private String extractEventType(String payload) {
        // Simple string-based extraction - in a production environment,
        // you'd want to use a proper JSON parser
        int typeIndex = payload.indexOf("\"type\":\"");
        if (typeIndex != -1) {
            int startIndex = typeIndex + 8;
            int endIndex = payload.indexOf("\"", startIndex);
            return payload.substring(startIndex, endIndex);
        }
        return null;
    }
    
    /**
     * Extract user ID from payload
     * @param payload The webhook payload
     * @return The user ID string
     */
    private String extractUserId(String payload) {
        // Simple string-based extraction
        int idIndex = payload.indexOf("\"id\":\"");
        if (idIndex != -1) {
            int startIndex = idIndex + 6;
            int endIndex = payload.indexOf("\"", startIndex);
            return payload.substring(startIndex, endIndex);
        }
        
        // Try alternative format
        idIndex = payload.indexOf("\"user_id\":\"");
        if (idIndex != -1) {
            int startIndex = idIndex + 11;
            int endIndex = payload.indexOf("\"", startIndex);
            return payload.substring(startIndex, endIndex);
        }
        
        return null;
    }
    
    /**
     * Extract username from payload
     * @param payload The webhook payload
     * @return The username string
     */
    private String extractUsername(String payload) {
        int usernameIndex = payload.indexOf("\"username\":\"");
        if (usernameIndex != -1) {
            int startIndex = usernameIndex + 12;
            int endIndex = payload.indexOf("\"", startIndex);
            return payload.substring(startIndex, endIndex);
        }
        return null;
    }
    
    /**
     * Extract email from payload
     * @param payload The webhook payload
     * @return The email string
     */
    private String extractEmail(String payload) {
        int emailIndex = payload.indexOf("\"email_addresses\":[");
        if (emailIndex != -1) {
            int emailValueIndex = payload.indexOf("\"email_address\":\"", emailIndex);
            if (emailValueIndex != -1) {
                int startIndex = emailValueIndex + 17;
                int endIndex = payload.indexOf("\"", startIndex);
                return payload.substring(startIndex, endIndex);
            }
        }
        return null;
    }
    
    /**
     * Extract first name from payload
     * @param payload The webhook payload
     * @return The first name string
     */
    private String extractFirstName(String payload) {
        int firstNameIndex = payload.indexOf("\"first_name\":\"");
        if (firstNameIndex != -1) {
            int startIndex = firstNameIndex + 14;
            int endIndex = payload.indexOf("\"", startIndex);
            return payload.substring(startIndex, endIndex);
        }
        return null;
    }
    
    /**
     * Extract last name from payload
     * @param payload The webhook payload
     * @return The last name string
     */
    private String extractLastName(String payload) {
        int lastNameIndex = payload.indexOf("\"last_name\":\"");
        if (lastNameIndex != -1) {
            int startIndex = lastNameIndex + 13;
            int endIndex = payload.indexOf("\"", startIndex);
            return payload.substring(startIndex, endIndex);
        }
        return null;
    }
    
    /**
     * Extract profile picture from payload
     * @param payload The webhook payload
     * @return The profile picture URL
     */
    private String extractProfilePicture(String payload) {
        int imageIndex = payload.indexOf("\"image_url\":\"");
        if (imageIndex != -1) {
            int startIndex = imageIndex + 13;
            int endIndex = payload.indexOf("\"", startIndex);
            return payload.substring(startIndex, endIndex);
        }
        return null;
    }
    
    /**
     * Extract session ID from payload
     * @param payload The webhook payload
     * @return The session ID string
     */
    private String extractSessionId(String payload) {
        int sessionIndex = payload.indexOf("\"id\":\"");
        if (sessionIndex != -1) {
            int startIndex = sessionIndex + 6;
            int endIndex = payload.indexOf("\"", startIndex);
            return payload.substring(startIndex, endIndex);
        }
        return null;
    }
    
    /**
     * Extract provider information from payload
     * @param payload The webhook payload
     * @return Map of provider names to provider IDs
     */
    private Map<String, String> extractProviderInfo(String payload) {
        Map<String, String> providerInfo = new HashMap<>();
        
        // This is a simplified approach - in production, use a proper JSON parser
        int oauthIndex = payload.indexOf("\"oauth_accounts\":[");
        if (oauthIndex != -1) {
            int endIndex = payload.indexOf("]", oauthIndex);
            String oauthSection = payload.substring(oauthIndex, endIndex);
            
            // Extract provider name
            int providerIndex = oauthSection.indexOf("\"provider\":\"");
            if (providerIndex != -1) {
                int startIndex = providerIndex + 12;
                int providerEndIndex = oauthSection.indexOf("\"", startIndex);
                String provider = oauthSection.substring(startIndex, providerEndIndex);
                
                // Extract provider ID
                int providerIdIndex = oauthSection.indexOf("\"provider_user_id\":\"");
                if (providerIdIndex != -1) {
                    int idStartIndex = providerIdIndex + 20;
                    int idEndIndex = oauthSection.indexOf("\"", idStartIndex);
                    String providerId = oauthSection.substring(idStartIndex, idEndIndex);
                    
                    providerInfo.put(provider, providerId);
                }
            }
        }
        
        return providerInfo;
    }
}