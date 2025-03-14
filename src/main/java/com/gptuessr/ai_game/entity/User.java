package com.gptuessr.ai_game.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "users")
public class User {
   
    @Id
    private String id;
   
    @Indexed(unique = true)
    private String username;
   
    private String email;
   
    // Clerk Integration fields
    @Indexed(unique = true)
    private String clerkUserId;
    
    private String clerkSessionId;
    
    private Map<String, String> providerInfo;
    
    // The password field might not be needed if using Clerk for authentication
    // but kept for backward compatibility
    private String password;
    
    // Personal information fields
    private String firstName;
    
    private String lastName;
    
    private String displayName;
    
    private String bio;
    
    private String country;
    
    private String language;
    
    private String timezone;
    
    private LocalDateTime birthDate;
   
    private String profilePicture;
    
    // Communication preferences
    private boolean emailNotifications;
    
    private boolean gameInviteNotifications;
    
    // Privacy settings
    private boolean publicProfile;
    
    // Account status and timestamps
    private LocalDateTime createdAt;
   
    private LocalDateTime lastLogin;
    
    private LocalDateTime lastActive;
    
    private boolean isVerified;
    
    private boolean isBanned;
    
    private String banReason;
   
    // Game statistics
    private List<String> gameHistory = new ArrayList<>();
    
    private Map<String, Integer> achievementsEarned = new HashMap<>();
   
    private int totalScore;
   
    private int gamesPlayed;
   
    private int gamesWon;
    
    private int gamesLost;
    
    private int totalCorrectGuesses;
    
    private int totalPromptsCreated;
    
    private double averageGuessAccuracy;
    
    private int personalBestScore;
    
    private String favoriteGameMode;
    
    // Social interactions
    private List<String> friends = new ArrayList<>();
    
    private List<String> blockedUsers = new ArrayList<>();
   
    private boolean isOnline;
    
    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
        this.lastActive = LocalDateTime.now();
        this.emailNotifications = true;
        this.gameInviteNotifications = true;
        this.publicProfile = true;
        this.isVerified = false;
        this.isBanned = false;
        this.providerInfo = new HashMap<>();
    }
   
    // Constructor for Clerk integration
    public User(String clerkUserId, String username, String email) {
        this();
        this.clerkUserId = clerkUserId;
        this.username = username;
        this.email = email;
        this.displayName = username;
        this.isVerified = true; // Since Clerk has already verified the email
        this.totalScore = 0;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.isOnline = false;
        this.totalCorrectGuesses = 0;
        this.totalPromptsCreated = 0;
        this.averageGuessAccuracy = 0.0;
        this.personalBestScore = 0;
    }
    
    // Extended constructor with personal information for Clerk
    public User(String clerkUserId, String username, String email, String firstName, String lastName) {
        this(clerkUserId, username, email);
        this.firstName = firstName;
        this.lastName = lastName;
        this.displayName = firstName != null && lastName != null ? firstName + " " + lastName : username;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getClerkUserId() {
        return clerkUserId;
    }
    
    public void setClerkUserId(String clerkUserId) {
        this.clerkUserId = clerkUserId;
    }
    
    public String getClerkSessionId() {
        return clerkSessionId;
    }
    
    public void setClerkSessionId(String clerkSessionId) {
        this.clerkSessionId = clerkSessionId;
    }
    
    public Map<String, String> getProviderInfo() {
        return providerInfo;
    }
    
    public void setProviderInfo(Map<String, String> providerInfo) {
        this.providerInfo = providerInfo;
    }
    
    public void addProviderInfo(String provider, String providerId) {
        this.providerInfo.put(provider, providerId);
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public LocalDateTime getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(LocalDateTime birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getProfilePicture() {
        return profilePicture;
    }
    
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
    
    public boolean isEmailNotifications() {
        return emailNotifications;
    }
    
    public void setEmailNotifications(boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }
    
    public boolean isGameInviteNotifications() {
        return gameInviteNotifications;
    }
    
    public void setGameInviteNotifications(boolean gameInviteNotifications) {
        this.gameInviteNotifications = gameInviteNotifications;
    }
    
    public boolean isPublicProfile() {
        return publicProfile;
    }
    
    public void setPublicProfile(boolean publicProfile) {
        this.publicProfile = publicProfile;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
        this.lastActive = lastLogin;
    }
    
    public LocalDateTime getLastActive() {
        return lastActive;
    }
    
    public void setLastActive(LocalDateTime lastActive) {
        this.lastActive = lastActive;
    }
    
    public boolean isVerified() {
        return isVerified;
    }
    
    public void setVerified(boolean verified) {
        isVerified = verified;
    }
    
    public boolean isBanned() {
        return isBanned;
    }
    
    public void setBanned(boolean banned) {
        isBanned = banned;
    }
    
    public String getBanReason() {
        return banReason;
    }
    
    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }
    
    public List<String> getGameHistory() {
        return gameHistory;
    }
    
    public void setGameHistory(List<String> gameHistory) {
        this.gameHistory = gameHistory;
    }
   
    public void addGameToHistory(String gameId) {
        this.gameHistory.add(gameId);
        this.incrementGamesPlayed();
    }
    
    public Map<String, Integer> getAchievementsEarned() {
        return achievementsEarned;
    }
    
    public void setAchievementsEarned(Map<String, Integer> achievementsEarned) {
        this.achievementsEarned = achievementsEarned;
    }
    
    public void addAchievement(String achievementId, int level) {
        this.achievementsEarned.put(achievementId, level);
    }
    
    public int getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
        if (totalScore > personalBestScore) {
            this.personalBestScore = totalScore;
        }
    }
   
    public void incrementTotalScore(int points) {
        this.totalScore += points;
        if (this.totalScore > this.personalBestScore) {
            this.personalBestScore = this.totalScore;
        }
    }
    
    public int getGamesPlayed() {
        return gamesPlayed;
    }
    
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }
   
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }
    
    public int getGamesWon() {
        return gamesWon;
    }
    
    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }
   
    public void incrementGamesWon() {
        this.gamesWon++;
    }
    
    public int getGamesLost() {
        return gamesLost;
    }
    
    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
    }
    
    public void incrementGamesLost() {
        this.gamesLost++;
    }
    
    public int getTotalCorrectGuesses() {
        return totalCorrectGuesses;
    }
    
    public void setTotalCorrectGuesses(int totalCorrectGuesses) {
        this.totalCorrectGuesses = totalCorrectGuesses;
        updateGuessAccuracy();
    }
    
    public void incrementTotalCorrectGuesses() {
        this.totalCorrectGuesses++;
        updateGuessAccuracy();
    }
    
    public int getTotalPromptsCreated() {
        return totalPromptsCreated;
    }
    
    public void setTotalPromptsCreated(int totalPromptsCreated) {
        this.totalPromptsCreated = totalPromptsCreated;
    }
    
    public void incrementTotalPromptsCreated() {
        this.totalPromptsCreated++;
    }
    
    public double getAverageGuessAccuracy() {
        return averageGuessAccuracy;
    }
    
    private void updateGuessAccuracy() {
        // Avoid division by zero
        if (this.gamesPlayed > 0) {
            this.averageGuessAccuracy = (double) this.totalCorrectGuesses / this.gamesPlayed;
        }
    }
    
    public int getPersonalBestScore() {
        return personalBestScore;
    }
    
    public void setPersonalBestScore(int personalBestScore) {
        this.personalBestScore = personalBestScore;
    }
    
    public String getFavoriteGameMode() {
        return favoriteGameMode;
    }
    
    public void setFavoriteGameMode(String favoriteGameMode) {
        this.favoriteGameMode = favoriteGameMode;
    }
    
    public List<String> getFriends() {
        return friends;
    }
    
    public void setFriends(List<String> friends) {
        this.friends = friends;
    }
    
    public void addFriend(String userId) {
        if (!this.friends.contains(userId)) {
            this.friends.add(userId);
        }
    }
    
    public void removeFriend(String userId) {
        this.friends.remove(userId);
    }
    
    public List<String> getBlockedUsers() {
        return blockedUsers;
    }
    
    public void setBlockedUsers(List<String> blockedUsers) {
        this.blockedUsers = blockedUsers;
    }
    
    public void blockUser(String userId) {
        if (!this.blockedUsers.contains(userId)) {
            this.blockedUsers.add(userId);
            // Remove from friends if they were a friend
            this.friends.remove(userId);
        }
    }
    
    public void unblockUser(String userId) {
        this.blockedUsers.remove(userId);
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setOnline(boolean online) {
        isOnline = online;
        if (online) {
            this.lastActive = LocalDateTime.now();
        }
    }
    
    // Helper methods
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else {
            return username;
        }
    }
    
    public double getWinRate() {
        return gamesPlayed > 0 ? (double) gamesWon / gamesPlayed * 100 : 0;
    }
    
    public boolean hasFriend(String userId) {
        return friends.contains(userId);
    }
    
    public boolean hasBlocked(String userId) {
        return blockedUsers.contains(userId);
    }
}