package com.gptuessr.ai_game.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO for sending lobby information to the frontend
 */
public class LobbyResponseDTO {
    
    private String id;
    private String lobbyCode;
    private String hostId;
    private String hostName;
    private List<PlayerDTO> players = new ArrayList<>();
    private int maxPlayers;
    private int numberOfRounds;
    private int timeLimit;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private List<String> gameSettings = new ArrayList<>();
    private boolean isPrivate;
    private String difficulty;
    private boolean isCurrentUserHost;
    private boolean isCurrentUserInLobby;
    
    // Constructors
    public LobbyResponseDTO() {
    }
    
    // Nested Player DTO
    public static class PlayerDTO {
        private String id;
        private String username;
        private String displayName;
        private String profilePicture;
        private boolean isHost;
        private boolean isReady;
        
        public PlayerDTO() {
        }
        
        public PlayerDTO(String id, String username, String displayName, String profilePicture, boolean isHost, boolean isReady) {
            this.id = id;
            this.username = username;
            this.displayName = displayName;
            this.profilePicture = profilePicture;
            this.isHost = isHost;
            this.isReady = isReady;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public void setProfilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
        }

        public boolean isHost() {
            return isHost;
        }

        public void setHost(boolean host) {
            isHost = host;
        }

        public boolean isReady() {
            return isReady;
        }

        public void setReady(boolean ready) {
            isReady = ready;
        }
    }
    
    // Factory method to create response from LobbyDTO and UserMap
    public static LobbyResponseDTO fromLobbyDTO(LobbyDTO lobbyDTO, Map<String, Map<String, Object>> userInfoMap, String currentUserId) {
        LobbyResponseDTO response = new LobbyResponseDTO();
        
        response.setId(lobbyDTO.getId());
        response.setLobbyCode(lobbyDTO.getLobbyCode());
        response.setHostId(lobbyDTO.getHostId());
        response.setMaxPlayers(lobbyDTO.getMaxPlayers());
        response.setNumberOfRounds(lobbyDTO.getNumberOfRounds());
        response.setTimeLimit(lobbyDTO.getTimeLimit());
        response.setStatus(lobbyDTO.getStatus());
        response.setCreatedAt(lobbyDTO.getCreatedAt());
        response.setStartedAt(lobbyDTO.getStartedAt());
        response.setEndedAt(lobbyDTO.getEndedAt());
        response.setGameSettings(lobbyDTO.getGameSettings());
        
        // Set host name if available
        if (userInfoMap.containsKey(lobbyDTO.getHostId())) {
            Map<String, Object> hostInfo = userInfoMap.get(lobbyDTO.getHostId());
            response.setHostName((String) hostInfo.get("displayName"));
        }
        
        // Set current user flags
        response.setCurrentUserHost(lobbyDTO.getHostId().equals(currentUserId));
        response.setCurrentUserInLobby(lobbyDTO.getPlayerIds().contains(currentUserId));
        
        // Convert player list
        for (String playerId : lobbyDTO.getPlayerIds()) {
            PlayerDTO playerDTO = new PlayerDTO();
            playerDTO.setId(playerId);
            playerDTO.setHost(playerId.equals(lobbyDTO.getHostId()));
            playerDTO.setReady(true); // Default to ready, adjust as needed
            
            // Add user details if available
            if (userInfoMap.containsKey(playerId)) {
                Map<String, Object> userInfo = userInfoMap.get(playerId);
                playerDTO.setUsername((String) userInfo.get("username"));
                playerDTO.setDisplayName((String) userInfo.get("displayName"));
                playerDTO.setProfilePicture((String) userInfo.get("profilePicture"));
            }
            
            response.getPlayers().add(playerDTO);
        }
        
        return response;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLobbyCode() {
        return lobbyCode;
    }

    public void setLobbyCode(String lobbyCode) {
        this.lobbyCode = lobbyCode;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public List<PlayerDTO> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerDTO> players) {
        this.players = players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getNumberOfRounds() {
        return numberOfRounds;
    }

    public void setNumberOfRounds(int numberOfRounds) {
        this.numberOfRounds = numberOfRounds;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public List<String> getGameSettings() {
        return gameSettings;
    }

    public void setGameSettings(List<String> gameSettings) {
        this.gameSettings = gameSettings;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isCurrentUserHost() {
        return isCurrentUserHost;
    }

    public void setCurrentUserHost(boolean currentUserHost) {
        isCurrentUserHost = currentUserHost;
    }

    public boolean isCurrentUserInLobby() {
        return isCurrentUserInLobby;
    }

    public void setCurrentUserInLobby(boolean currentUserInLobby) {
        isCurrentUserInLobby = currentUserInLobby;
    }
}
