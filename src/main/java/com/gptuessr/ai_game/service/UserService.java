package com.gptuessr.ai_game.service;

import com.gptuessr.ai_game.entity.User;
import com.gptuessr.ai_game.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Register a new user from Clerk authentication
     * @param clerkUserId The unique user ID from Clerk
     * @param username The username
     * @param email The email address
     * @param firstName The first name (optional)
     * @param lastName The last name (optional)
     * @param profilePicture The profile picture URL (optional)
     * @param providerInfo Map of authentication providers and their IDs
     * @return The newly created user
     */
    public User registerClerkUser(String clerkUserId, String username, String email, 
                                String firstName, String lastName, String profilePicture, 
                                Map<String, String> providerInfo) {
        
        logger.info("Registering new user from Clerk with ID: {}", clerkUserId);
        
        // Check if user already exists by Clerk ID
        Optional<User> existingUserByClerkId = userRepository.findByClerkUserId(clerkUserId);
        if (existingUserByClerkId.isPresent()) {
            logger.info("User already exists with Clerk ID: {}", clerkUserId);
            return existingUserByClerkId.get();
        }
        
        // Check if username or email already exists
        if (userRepository.existsByUsername(username)) {
            logger.warn("Username '{}' already exists. Generating unique username.", username);
            username = generateUniqueUsername(username);
        }
        
        // Create new user
        User newUser = new User(clerkUserId, username, email);
        
        // Set additional user details if provided
        if (firstName != null) newUser.setFirstName(firstName);
        if (lastName != null) newUser.setLastName(lastName);
        if (profilePicture != null) newUser.setProfilePicture(profilePicture);
        
        // Set display name based on available information
        if (firstName != null && lastName != null) {
            newUser.setDisplayName(firstName + " " + lastName);
        } else if (firstName != null) {
            newUser.setDisplayName(firstName);
        }
        
        // Set provider information
        if (providerInfo != null) {
            for (Map.Entry<String, String> entry : providerInfo.entrySet()) {
                newUser.addProviderInfo(entry.getKey(), entry.getValue());
            }
        }
        
        // Set initial login time
        newUser.setLastLogin(LocalDateTime.now());
        
        // Save the user to the database
        User savedUser = userRepository.save(newUser);
        logger.info("Successfully registered new user with Clerk ID: {}", clerkUserId);
        
        return savedUser;
    }
    
    /**
     * Update user information on login
     * @param clerkUserId The unique user ID from Clerk
     * @param clerkSessionId The current session ID
     * @return The updated user or null if user not found
     */
    public User updateUserOnLogin(String clerkUserId, String clerkSessionId) {
        logger.info("Updating user on login with Clerk ID: {}", clerkUserId);
        
        Optional<User> existingUser = userRepository.findByClerkUserId(clerkUserId);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            
            // Update session and login info
            user.setClerkSessionId(clerkSessionId);
            user.setLastLogin(LocalDateTime.now());
            user.setLastActive(LocalDateTime.now());
            user.setOnline(true);
            
            User updatedUser = userRepository.save(user);
            logger.info("Successfully updated user login information for Clerk ID: {}", clerkUserId);
            
            return updatedUser;
        } else {
            logger.warn("Failed to update user on login. User not found with Clerk ID: {}", clerkUserId);
            return null;
        }
    }
    
    /**
     * Update user information from Clerk webhook or API
     * @param clerkUserId The unique user ID from Clerk
     * @param userData Map containing updated user data
     * @return The updated user or null if user not found
     */
    public User updateUserInfo(String clerkUserId, Map<String, Object> userData) {
        logger.info("Updating user information for Clerk ID: {}", clerkUserId);
        
        Optional<User> existingUser = userRepository.findByClerkUserId(clerkUserId);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            
            // Update user information based on provided data
            if (userData.containsKey("email")) {
                user.setEmail((String) userData.get("email"));
            }
            
            if (userData.containsKey("username")) {
                String newUsername = (String) userData.get("username");
                if (!user.getUsername().equals(newUsername) && !userRepository.existsByUsername(newUsername)) {
                    user.setUsername(newUsername);
                }
            }
            
            if (userData.containsKey("firstName")) {
                user.setFirstName((String) userData.get("firstName"));
            }
            
            if (userData.containsKey("lastName")) {
                user.setLastName((String) userData.get("lastName"));
            }
            
            if (userData.containsKey("profilePicture")) {
                user.setProfilePicture((String) userData.get("profilePicture"));
            }
            
            // Update display name if first or last name changed
            if (userData.containsKey("firstName") || userData.containsKey("lastName")) {
                updateDisplayName(user);
            }
            
            // Save the updated user
            User updatedUser = userRepository.save(user);
            logger.info("Successfully updated user information for Clerk ID: {}", clerkUserId);
            
            return updatedUser;
        } else {
            logger.warn("Failed to update user information. User not found with Clerk ID: {}", clerkUserId);
            return null;
        }
    }
    
    /**
     * Update a user's online status to offline
     * @param clerkUserId The unique user ID from Clerk
     * @return The updated user or null if user not found
     */
    public User updateUserLogout(String clerkUserId) {
        logger.info("Logging out user with Clerk ID: {}", clerkUserId);
        
        Optional<User> existingUser = userRepository.findByClerkUserId(clerkUserId);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            
            user.setOnline(false);
            user.setClerkSessionId(null);
            
            User updatedUser = userRepository.save(user);
            logger.info("Successfully logged out user with Clerk ID: {}", clerkUserId);
            
            return updatedUser;
        } else {
            logger.warn("Failed to log out user. User not found with Clerk ID: {}", clerkUserId);
            return null;
        }
    }
    
    /**
     * Find a user by Clerk user ID
     * @param clerkUserId The unique user ID from Clerk
     * @return Optional containing the user if found
     */
    public Optional<User> findByClerkUserId(String clerkUserId) {
        return userRepository.findByClerkUserId(clerkUserId);
    }
    
    /**
     * Generate a unique username by appending a number if necessary
     * @param baseUsername The original username to make unique
     * @return A unique username
     */
    private String generateUniqueUsername(String baseUsername) {
        String uniqueUsername = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(uniqueUsername)) {
            uniqueUsername = baseUsername + counter;
            counter++;
        }
        
        return uniqueUsername;
    }
    
    /**
     * Update a user's display name based on first and last name
     * @param user The user to update
     */
    private void updateDisplayName(User user) {
        if (user.getFirstName() != null && user.getLastName() != null) {
            user.setDisplayName(user.getFirstName() + " " + user.getLastName());
        } else if (user.getFirstName() != null) {
            user.setDisplayName(user.getFirstName());
        } else if (user.getLastName() != null) {
            user.setDisplayName(user.getLastName());
        }
    }
}