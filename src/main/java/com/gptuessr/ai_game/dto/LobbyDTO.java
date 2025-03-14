package com.gptuessr.ai_game.dto;

import com.gptuessr.ai_game.entity.Lobby;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Lobby entity
 */
public class LobbyDTO {
    
    private String id;
    
    private String lobbyCode;
    
    private String hostId;
    
    private List<String> playerIds = new ArrayList<>();
    
    @Min(value = 2, message = "Maximum players must be at least 2")
    @Max(value = 1000, message = "Maximum players cannot exceed 1000")
    private int maxPlayers;
    
    @Min(value = 1, message = "Number of rounds must be at least 1")
    @Max(value = 20, message = "Number of rounds cannot exceed 20")
    private int numberOfRounds;
    
    @Min(value = 1, message = "Time limit must be at least 1 second")
    @Max(value = 180, message = "Time limit cannot exceed 180 seconds")
    private int timeLimit;
    
    private String status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime startedAt;
    
    private LocalDateTime endedAt;
    
    private List<String> gameSettings = new ArrayList<>();
    
    private int playerCount;
    
    private boolean isPlayerInLobby;
    
    private boolean isHost;
    
    // Constructors
    public LobbyDTO() {
    }
    
    public LobbyDTO(Lobby lobby) {
        this.id = lobby.getId();
        this.lobbyCode = lobby.getLobbyCode();
        this.hostId = lobby.getHostId();
        this.playerIds = new ArrayList<>(lobby.getPlayerIds());
        this.maxPlayers = lobby.getMaxPlayers();
        this.numberOfRounds = lobby.getNumberOfRounds();
        this.timeLimit = lobby.getTimeLimit();
        this.status = lobby.getStatus().toString();
        this.createdAt = lobby.getCreatedAt();
        this.startedAt = lobby.getStartedAt();
        this.endedAt = lobby.getEndedAt();
        this.gameSettings = new ArrayList<>(lobby.getGameSettings());
        this.playerCount = lobby.getPlayerIds().size();
    }
    
    // Convert LobbyDTO to Lobby entity
    public Lobby toEntity() {
        Lobby lobby = new Lobby();
        if (this.id != null) {
            lobby.setId(this.id);
        }
        lobby.setLobbyCode(this.lobbyCode);
        lobby.setHostId(this.hostId);
        lobby.setPlayerIds(new ArrayList<>(this.playerIds));
        lobby.setMaxPlayers(this.maxPlayers);
        lobby.setNumberOfRounds(this.numberOfRounds);
        lobby.setTimeLimit(this.timeLimit);
        if (this.status != null) {
            lobby.setStatus(Lobby.GameStatus.valueOf(this.status));
        }
        if (this.createdAt != null) {
            lobby.setCreatedAt(this.createdAt);
        }
        if (this.startedAt != null) {
            lobby.setStartedAt(this.startedAt);
        }
        if (this.endedAt != null) {
            lobby.setEndedAt(this.endedAt);
        }
        lobby.setGameSettings(new ArrayList<>(this.gameSettings));
        return lobby;
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

    public List<String> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<String> playerIds) {
        this.playerIds = playerIds;
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
    
    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public boolean isPlayerInLobby() {
        return isPlayerInLobby;
    }

    public void setPlayerInLobby(boolean playerInLobby) {
        isPlayerInLobby = playerInLobby;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }
}