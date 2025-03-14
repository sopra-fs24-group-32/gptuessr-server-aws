package com.gptuessr.ai_game.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "lobbies")
public class Lobby {
    
    @Id
    private String id;
    
    private String lobbyCode;
    
    private String hostId;
    
    private List<String> playerIds = new ArrayList<>();
    
    private int maxPlayers;
    
    private int numberOfRounds;
    
    private int timeLimit;
    
    private GameStatus status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime startedAt;
    
    private LocalDateTime endedAt;
    
    private List<String> gameSettings = new ArrayList<>();
    
    // Constructors
    public Lobby() {
        this.createdAt = LocalDateTime.now();
        this.status = GameStatus.WAITING;
    }
    
    public Lobby(String hostId, String lobbyCode, int numberOfRounds, int timeLimit) {
        this.hostId = hostId;
        this.lobbyCode = lobbyCode;
        this.numberOfRounds = numberOfRounds;
        this.timeLimit = timeLimit;
        this.playerIds = new ArrayList<>();
        this.playerIds.add(hostId);
        this.maxPlayers = 10; // Default value
        this.status = GameStatus.WAITING;
        this.createdAt = LocalDateTime.now();
    }
    
    // Enum for game status
    public enum GameStatus {
        WAITING,
        IN_PROGRESS,
        FINISHED,
        CLOSED
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
    
    public void addPlayer(String playerId) {
        if (!this.playerIds.contains(playerId)) {
            this.playerIds.add(playerId);
        }
    }
    
    public void removePlayer(String playerId) {
        this.playerIds.remove(playerId);
    }
    
    public boolean containsPlayer(String playerId) {
        return this.playerIds.contains(playerId);
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

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
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
    
    public void addGameSetting(String setting) {
        this.gameSettings.add(setting);
    }
    
    public boolean isLobbyFull() {
        return this.playerIds.size() >= this.maxPlayers;
    }
    
    public boolean hasMinimumPlayers() {
        return this.playerIds.size() >= 3; // As per requirement S21
    }
}