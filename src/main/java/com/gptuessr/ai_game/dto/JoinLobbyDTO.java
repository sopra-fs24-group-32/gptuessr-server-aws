package com.gptuessr.ai_game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for joining a lobby
 */
public class JoinLobbyDTO {
    
    @NotBlank(message = "Lobby code is required")
    @Pattern(regexp = "^[A-Z0-9]{6}$", message = "Lobby code must be 6 alphanumeric characters")
    private String lobbyCode;
    
    // Constructors
    public JoinLobbyDTO() {
    }
    
    public JoinLobbyDTO(String lobbyCode) {
        this.lobbyCode = lobbyCode;
    }
    
    // Getters and Setters
    public String getLobbyCode() {
        return lobbyCode;
    }

    public void setLobbyCode(String lobbyCode) {
        this.lobbyCode = lobbyCode;
    }
}