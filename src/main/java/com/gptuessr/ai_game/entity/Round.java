package com.gptuessr.ai_game.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Round {
    
    private int roundNumber;
    
    private String prompterId;
    
    private String promptText;
    
    private String generatedImageUrl;
    
    private Map<String, Guess> playerGuesses = new HashMap<>();
    
    private RoundStatus status;
    
    private LocalDateTime startedAt;
    
    private LocalDateTime endedAt;
    
    private int timeLimit;
    
    // Constructor
    public Round() {
        this.status = RoundStatus.WAITING_FOR_PROMPT;
        this.startedAt = LocalDateTime.now();
    }
    
    public Round(int roundNumber, String prompterId, int timeLimit) {
        this.roundNumber = roundNumber;
        this.prompterId = prompterId;
        this.timeLimit = timeLimit;
        this.status = RoundStatus.WAITING_FOR_PROMPT;
        this.startedAt = LocalDateTime.now();
    }
    
    // Enum for round status
    public enum RoundStatus {
        WAITING_FOR_PROMPT,
        GENERATING_IMAGE,
        WAITING_FOR_GUESSES,
        EVALUATING_GUESSES,
        COMPLETED
    }
    
    // Getters and Setters
    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getPrompterId() {
        return prompterId;
    }

    public void setPrompterId(String prompterId) {
        this.prompterId = prompterId;
    }

    public String getPromptText() {
        return promptText;
    }

    public void setPromptText(String promptText) {
        this.promptText = promptText;
    }

    public String getGeneratedImageUrl() {
        return generatedImageUrl;
    }

    public void setGeneratedImageUrl(String generatedImageUrl) {
        this.generatedImageUrl = generatedImageUrl;
    }

    public Map<String, Guess> getPlayerGuesses() {
        return playerGuesses;
    }

    public void setPlayerGuesses(Map<String, Guess> playerGuesses) {
        this.playerGuesses = playerGuesses;
    }
    
    public void addPlayerGuess(String playerId, Guess guess) {
        this.playerGuesses.put(playerId, guess);
    }

    public RoundStatus getStatus() {
        return status;
    }

    public void setStatus(RoundStatus status) {
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

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
    
    public List<Map.Entry<String, Integer>> getRoundRanking() {
        Map<String, Integer> roundScores = new HashMap<>();
        
        for (Map.Entry<String, Guess> entry : this.playerGuesses.entrySet()) {
            roundScores.put(entry.getKey(), entry.getValue().getScore());
        }
        
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(roundScores.entrySet());
        sortedEntries.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        return sortedEntries;
    }
    
    public String getBestGuesserId() {
        String bestGuesserId = null;
        int highestScore = -1;
        
        for (Map.Entry<String, Guess> entry : this.playerGuesses.entrySet()) {
            if (entry.getValue().getScore() > highestScore) {
                highestScore = entry.getValue().getScore();
                bestGuesserId = entry.getKey();
            }
        }
        
        return bestGuesserId;
    }
    
    public boolean areAllGuessesSubmitted(List<String> playerIds) {
        // Exclude the prompter from the count
        int expectedGuesses = playerIds.size() - 1;
        return this.playerGuesses.size() >= expectedGuesses;
    }
}
