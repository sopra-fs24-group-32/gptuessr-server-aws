package com.gptuessr.ai_game.entity;

import java.time.LocalDateTime;

public class Guess {
    
    private String playerId;
    
    private String guessText;
    
    private int score;
    
    private double accuracy;
    
    private LocalDateTime submittedAt;
    
    private long responseTimeMs;
    
    // Constructors
    public Guess() {
        this.submittedAt = LocalDateTime.now();
    }
    
    public Guess(String playerId, String guessText, long responseTimeMs) {
        this.playerId = playerId;
        this.guessText = guessText;
        this.responseTimeMs = responseTimeMs;
        this.submittedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getGuessText() {
        return guessText;
    }

    public void setGuessText(String guessText) {
        this.guessText = guessText;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
}