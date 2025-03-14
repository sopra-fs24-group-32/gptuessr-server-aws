package com.gptuessr.ai_game.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "games")
public class Game {
    
    @Id
    private String id;
    
    private String lobbyId;
    
    private List<String> playerIds = new ArrayList<>();
    
    private int currentRound;
    
    private int totalRounds;
    
    private String currentPrompter;
    
    private List<Round> rounds = new ArrayList<>();
    
    private Map<String, Integer> playerScores = new HashMap<>();
    
    private GameStatus status;
    
    private LocalDateTime startedAt;
    
    private LocalDateTime endedAt;
    
    // Constructors
    public Game() {
        this.startedAt = LocalDateTime.now();
        this.status = GameStatus.IN_PROGRESS;
        this.currentRound = 1;
    }
    
    public Game(String lobbyId, List<String> playerIds, int totalRounds) {
        this.lobbyId = lobbyId;
        this.playerIds = playerIds;
        this.totalRounds = totalRounds;
        this.startedAt = LocalDateTime.now();
        this.status = GameStatus.IN_PROGRESS;
        this.currentRound = 1;
        
        // Initialize scores for all players
        for (String playerId : playerIds) {
            this.playerScores.put(playerId, 0);
        }
    }
    
    // Enum for game status
    public enum GameStatus {
        IN_PROGRESS,
        FINISHED,
        ABORTED
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    public List<String> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<String> playerIds) {
        this.playerIds = playerIds;
    }
    
    public void removePlayer(String playerId) {
        this.playerIds.remove(playerId);
        this.playerScores.remove(playerId);
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }
    
    public void incrementCurrentRound() {
        this.currentRound++;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }

    public String getCurrentPrompter() {
        return currentPrompter;
    }

    public void setCurrentPrompter(String currentPrompter) {
        this.currentPrompter = currentPrompter;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }
    
    public void addRound(Round round) {
        this.rounds.add(round);
    }

    public Map<String, Integer> getPlayerScores() {
        return playerScores;
    }

    public void setPlayerScores(Map<String, Integer> playerScores) {
        this.playerScores = playerScores;
    }
    
    public void updatePlayerScore(String playerId, int scoreToAdd) {
        int currentScore = this.playerScores.getOrDefault(playerId, 0);
        this.playerScores.put(playerId, currentScore + scoreToAdd);
    }
    
    public String getWinner() {
        if (this.status != GameStatus.FINISHED) {
            return null;
        }
        
        String winnerId = null;
        int highestScore = -1;
        
        for (Map.Entry<String, Integer> entry : this.playerScores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                winnerId = entry.getKey();
            }
        }
        
        return winnerId;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
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
    
    public boolean isGameOver() {
        return this.currentRound > this.totalRounds;
    }
    
    public List<Map.Entry<String, Integer>> getFinalRanking() {
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(this.playerScores.entrySet());
        sortedEntries.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        return sortedEntries;
    }
}
