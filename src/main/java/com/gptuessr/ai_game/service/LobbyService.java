package com.gptuessr.ai_game.service;

import com.gptuessr.ai_game.entity.Lobby;
import com.gptuessr.ai_game.entity.User;
import com.gptuessr.ai_game.repository.LobbyRepository;
import com.gptuessr.ai_game.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class LobbyService {

    private static final Logger logger = LoggerFactory.getLogger(LobbyService.class);
    
    @Autowired
    private LobbyRepository lobbyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create a new game lobby
     * @param hostId The user ID of the host
     * @param numberOfRounds The number of rounds for the game
     * @param timeLimit The time limit per round in seconds
     * @param maxPlayers The maximum number of players allowed
     * @param gameSettings Additional game settings
     * @return The created lobby
     */
    public Lobby createLobby(String hostId, int numberOfRounds, int timeLimit, int maxPlayers, List<String> gameSettings) {
        logger.info("Creating new lobby for host: {}", hostId);
        
        // Verify host exists
        Optional<User> hostUser = userRepository.findByClerkUserId(hostId);
        if (hostUser.isEmpty()) {
            logger.error("Cannot create lobby: Host user not found with ID: {}", hostId);
            throw new IllegalArgumentException("Host user not found");
        }
        
        // Generate unique lobby code
        String lobbyCode = generateUniqueLobbyCode();
        
        // Create new lobby
        Lobby lobby = new Lobby(hostId, lobbyCode, numberOfRounds, timeLimit);
        lobby.setMaxPlayers(maxPlayers);
        
        // Add game settings if provided
        if (gameSettings != null && !gameSettings.isEmpty()) {
            lobby.setGameSettings(gameSettings);
        }
        
        // Save and return the lobby
        Lobby savedLobby = lobbyRepository.save(lobby);
        logger.info("Successfully created lobby with code: {} for host: {}", lobbyCode, hostId);
        
        return savedLobby;
    }
    
    /**
     * Generate a unique lobby code
     * @return A unique lobby code
     */
    private String generateUniqueLobbyCode() {
        Random random = new Random();
        String lobbyCode;
        boolean isUnique = false;
        
        // Keep generating until we find a unique code
        do {
            StringBuilder codeBuilder = new StringBuilder();
            String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
            
            // Generate a 6-character code
            for (int i = 0; i < 6; i++) {
                codeBuilder.append(characters.charAt(random.nextInt(characters.length())));
            }
            
            lobbyCode = codeBuilder.toString();
            isUnique = !lobbyRepository.existsByLobbyCode(lobbyCode);
            
        } while (!isUnique);
        
        return lobbyCode;
    }
    
    /**
     * Find a lobby by its code
     * @param lobbyCode The lobby code
     * @return Optional containing the lobby if found
     */
    public Optional<Lobby> findByLobbyCode(String lobbyCode) {
        return lobbyRepository.findByLobbyCode(lobbyCode);
    }
    
    /**
     * Find lobbies for a host
     * @param hostId The host user ID
     * @return List of lobbies created by the host
     */
    public List<Lobby> findLobbiesByHost(String hostId) {
        return lobbyRepository.findByHostId(hostId);
    }
    
    /**
     * Find lobbies that a player is in
     * @param playerId The player user ID
     * @return List of lobbies the player is in
     */
    public List<Lobby> findLobbiesByPlayer(String playerId) {
        return lobbyRepository.findByPlayerIdsContaining(playerId);
    }
    
    /**
     * Join a lobby
     * @param lobbyCode The lobby code
     * @param playerId The player user ID
     * @return The updated lobby
     * @throws IllegalArgumentException if lobby is full or player is already in lobby
     */
    public Lobby joinLobby(String lobbyCode, String playerId) {
        logger.info("Player {} attempting to join lobby: {}", playerId, lobbyCode);
        
        // Find lobby
        Optional<Lobby> optionalLobby = lobbyRepository.findByLobbyCode(lobbyCode);
        if (optionalLobby.isEmpty()) {
            logger.error("Cannot join lobby: Lobby not found with code: {}", lobbyCode);
            throw new IllegalArgumentException("Lobby not found");
        }
        
        Lobby lobby = optionalLobby.get();
        
        // Check if lobby is in WAITING status
        if (lobby.getStatus() != Lobby.GameStatus.WAITING) {
            logger.error("Cannot join lobby: Lobby is not in WAITING status. Current status: {}", lobby.getStatus());
            throw new IllegalArgumentException("Cannot join lobby: Game already in progress or finished");
        }
        
        // Check if lobby is full
        if (lobby.isLobbyFull()) {
            logger.error("Cannot join lobby: Lobby is full. Current players: {}, Max: {}", 
                lobby.getPlayerIds().size(), lobby.getMaxPlayers());
            throw new IllegalArgumentException("Lobby is full");
        }
        
        // Check if player is already in lobby
        if (lobby.containsPlayer(playerId)) {
            logger.info("Player {} is already in lobby {}", playerId, lobbyCode);
            return lobby;
        }
        
        // Verify player exists
        Optional<User> playerUser = userRepository.findById(playerId);
        if (playerUser.isEmpty()) {
            logger.error("Cannot join lobby: Player user not found with ID: {}", playerId);
            throw new IllegalArgumentException("Player user not found");
        }
        
        // Add player to lobby
        lobby.addPlayer(playerId);
        
        // Save and return updated lobby
        Lobby updatedLobby = lobbyRepository.save(lobby);
        logger.info("Player {} successfully joined lobby {}", playerId, lobbyCode);
        
        return updatedLobby;
    }
    
    /**
     * Leave a lobby
     * @param lobbyCode The lobby code
     * @param playerId The player user ID
     * @return The updated lobby or null if lobby should be closed
     */
    public Lobby leaveLobby(String lobbyCode, String playerId) {
        logger.info("Player {} attempting to leave lobby: {}", playerId, lobbyCode);
        
        // Find lobby
        Optional<Lobby> optionalLobby = lobbyRepository.findByLobbyCode(lobbyCode);
        if (optionalLobby.isEmpty()) {
            logger.error("Cannot leave lobby: Lobby not found with code: {}", lobbyCode);
            throw new IllegalArgumentException("Lobby not found");
        }
        
        Lobby lobby = optionalLobby.get();
        
        // Check if player is in lobby
        if (!lobby.containsPlayer(playerId)) {
            logger.error("Cannot leave lobby: Player {} not in lobby {}", playerId, lobbyCode);
            throw new IllegalArgumentException("Player not in lobby");
        }
        
        // Check if player is the host
        boolean isHost = playerId.equals(lobby.getHostId());
        
        if (isHost) {
            // If host leaves, close the lobby
            logger.info("Host {} left lobby {}. Closing lobby.", playerId, lobbyCode);
            closeLobby(lobbyCode);
            return null;
        } else {
            // Remove player from lobby
            lobby.removePlayer(playerId);
            
            // Save and return updated lobby
            Lobby updatedLobby = lobbyRepository.save(lobby);
            logger.info("Player {} successfully left lobby {}", playerId, lobbyCode);
            
            return updatedLobby;
        }
    }
    
    /**
     * Start the game
     * @param lobbyCode The lobby code
     * @param hostId The host user ID
     * @return The updated lobby
     * @throws IllegalArgumentException if minimum requirements not met
     */
    public Lobby startGame(String lobbyCode, String hostId) {
        logger.info("Host {} attempting to start game in lobby: {}", hostId, lobbyCode);
        
        // Find lobby
        Optional<Lobby> optionalLobby = lobbyRepository.findByLobbyCode(lobbyCode);
        if (optionalLobby.isEmpty()) {
            logger.error("Cannot start game: Lobby not found with code: {}", lobbyCode);
            throw new IllegalArgumentException("Lobby not found");
        }
        
        Lobby lobby = optionalLobby.get();
        
        // Verify that the request is from the host
        if (!hostId.equals(lobby.getHostId())) {
            logger.error("Cannot start game: User {} is not the host of lobby {}", hostId, lobbyCode);
            throw new IllegalArgumentException("Only the host can start the game");
        }
        
        // Check if lobby has minimum required players
        if (!lobby.hasMinimumPlayers()) {
            logger.error("Cannot start game: Not enough players. Current players: {}", lobby.getPlayerIds().size());
            throw new IllegalArgumentException("Not enough players to start the game (minimum 3)");
        }
        
        // Update lobby status and start time
        lobby.setStatus(Lobby.GameStatus.IN_PROGRESS);
        lobby.setStartedAt(LocalDateTime.now());
        
        // Save and return updated lobby
        Lobby updatedLobby = lobbyRepository.save(lobby);
        logger.info("Game successfully started in lobby {}", lobbyCode);
        
        return updatedLobby;
    }
    
    /**
     * End the game
     * @param lobbyCode The lobby code
     * @return The updated lobby
     */
    public Lobby endGame(String lobbyCode) {
        logger.info("Ending game in lobby: {}", lobbyCode);
        
        // Find lobby
        Optional<Lobby> optionalLobby = lobbyRepository.findByLobbyCode(lobbyCode);
        if (optionalLobby.isEmpty()) {
            logger.error("Cannot end game: Lobby not found with code: {}", lobbyCode);
            throw new IllegalArgumentException("Lobby not found");
        }
        
        Lobby lobby = optionalLobby.get();
        
        // Update lobby status and end time
        lobby.setStatus(Lobby.GameStatus.FINISHED);
        lobby.setEndedAt(LocalDateTime.now());
        
        // Save and return updated lobby
        Lobby updatedLobby = lobbyRepository.save(lobby);
        logger.info("Game successfully ended in lobby {}", lobbyCode);
        
        return updatedLobby;
    }
    
    /**
     * Close a lobby
     * @param lobbyCode The lobby code
     */
    public void closeLobby(String lobbyCode) {
        logger.info("Closing lobby: {}", lobbyCode);
        
        // Find lobby
        Optional<Lobby> optionalLobby = lobbyRepository.findByLobbyCode(lobbyCode);
        if (optionalLobby.isEmpty()) {
            logger.error("Cannot close lobby: Lobby not found with code: {}", lobbyCode);
            throw new IllegalArgumentException("Lobby not found");
        }
        
        Lobby lobby = optionalLobby.get();
        
        // Update lobby status
        lobby.setStatus(Lobby.GameStatus.CLOSED);
        
        // Save the updated lobby
        lobbyRepository.save(lobby);
        logger.info("Lobby {} successfully closed", lobbyCode);
    }
    
    /**
     * Update lobby settings
     * @param lobbyCode The lobby code
     * @param hostId The host user ID
     * @param numberOfRounds The number of rounds
     * @param timeLimit The time limit per round in seconds
     * @param maxPlayers The maximum number of players
     * @param gameSettings Additional game settings
     * @return The updated lobby
     */
    public Lobby updateLobbySettings(String lobbyCode, String hostId, Integer numberOfRounds, 
                                    Integer timeLimit, Integer maxPlayers, List<String> gameSettings) {
        logger.info("Updating settings for lobby: {}", lobbyCode);
        
        // Find lobby
        Optional<Lobby> optionalLobby = lobbyRepository.findByLobbyCode(lobbyCode);
        if (optionalLobby.isEmpty()) {
            logger.error("Cannot update lobby: Lobby not found with code: {}", lobbyCode);
            throw new IllegalArgumentException("Lobby not found");
        }
        
        Lobby lobby = optionalLobby.get();
        
        // Verify that the request is from the host
        if (!hostId.equals(lobby.getHostId())) {
            logger.error("Cannot update lobby: User {} is not the host of lobby {}", hostId, lobbyCode);
            throw new IllegalArgumentException("Only the host can update lobby settings");
        }
        
        // Check if game has already started
        if (lobby.getStatus() != Lobby.GameStatus.WAITING) {
            logger.error("Cannot update lobby: Game has already started or lobby is closed");
            throw new IllegalArgumentException("Cannot update settings after game has started");
        }
        
        // Update settings
        if (numberOfRounds != null) {
            lobby.setNumberOfRounds(numberOfRounds);
        }
        
        if (timeLimit != null) {
            lobby.setTimeLimit(timeLimit);
        }
        
        if (maxPlayers != null) {
            // Ensure max players is not less than current players
            if (maxPlayers < lobby.getPlayerIds().size()) {
                logger.error("Cannot set max players to less than current players count");
                throw new IllegalArgumentException("Max players cannot be less than current player count");
            }
            lobby.setMaxPlayers(maxPlayers);
        }
        
        if (gameSettings != null) {
            lobby.setGameSettings(gameSettings);
        }
        
        // Save and return updated lobby
        Lobby updatedLobby = lobbyRepository.save(lobby);
        logger.info("Successfully updated settings for lobby {}", lobbyCode);
        
        return updatedLobby;
    }
    
    /**
     * Clean up old lobbies
     * Scheduled to run daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupOldLobbies() {
        logger.info("Running scheduled cleanup of old lobbies");
        
        // Find lobbies in WAITING status that are older than 24 hours
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<Lobby> oldLobbies = lobbyRepository.findByStatusAndCreatedAtBefore(Lobby.GameStatus.WAITING, oneDayAgo);
        
        logger.info("Found {} old lobbies to clean up", oldLobbies.size());
        
        for (Lobby lobby : oldLobbies) {
            lobby.setStatus(Lobby.GameStatus.CLOSED);
            lobbyRepository.save(lobby);
            logger.info("Closed inactive lobby: {}", lobby.getLobbyCode());
        }
    }
    
    /**
     * Get active lobbies count
     * @return The number of active lobbies
     */
    public long getActiveLobbiesCount() {
        return lobbyRepository.countByStatus(Lobby.GameStatus.WAITING) + 
               lobbyRepository.countByStatus(Lobby.GameStatus.IN_PROGRESS);
    }
}