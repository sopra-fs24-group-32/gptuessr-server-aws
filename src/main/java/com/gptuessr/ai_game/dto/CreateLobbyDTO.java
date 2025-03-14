package com.gptuessr.ai_game.dto;

import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for creating a new lobby
 */
public class CreateLobbyDTO {
    
    @NotNull(message = "Number of rounds is required")
    @Min(value = 1, message = "Number of rounds must be at least 1")
    @Max(value = 20, message = "Number of rounds cannot exceed 20")
    private Integer numberOfRounds;
    
    @NotNull(message = "Time limit is required")
    @Min(value = 1, message = "Time limit must be at least 1 second")
    @Max(value = 180, message = "Time limit cannot exceed 180 seconds")
    private Integer timeLimit;
    
    @Min(value = 2, message = "Maximum players must be at least 2")
    @Max(value = 1000, message = "Maximum players cannot exceed 1000")
    private Integer maxPlayers = 10; // Default value
    
    private List<String> gameSettings = new ArrayList<>();
    
    private Boolean isPrivate = true;
    
    private String difficulty = "medium";
    
    // Constructors
    public CreateLobbyDTO() {
    }
    
    // Getters and Setters
    public Integer getNumberOfRounds() {
        return numberOfRounds;
    }

    public void setNumberOfRounds(Integer numberOfRounds) {
        this.numberOfRounds = numberOfRounds;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public List<String> getGameSettings() {
        return gameSettings;
    }

    public void setGameSettings(List<String> gameSettings) {
        this.gameSettings = gameSettings;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}