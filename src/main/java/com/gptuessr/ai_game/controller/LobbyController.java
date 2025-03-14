package com.gptuessr.ai_game.controller;

import com.gptuessr.ai_game.dto.*;
import com.gptuessr.ai_game.entity.Lobby;
import com.gptuessr.ai_game.entity.User;
import com.gptuessr.ai_game.service.LobbyService;
import com.gptuessr.ai_game.service.UserService;
import com.gptuessr.ai_game.util.ClerkAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lobbies")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS
}, allowCredentials = "true")
@Validated
public class LobbyController {

    private static final Logger logger = LoggerFactory.getLogger(LobbyController.class);
    
    @Autowired
    private LobbyService lobbyService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ClerkAuthUtils authUtils;
    
    /**
     * Create a new game lobby
     * @param createLobbyDTO DTO containing lobby creation data
     * @param request HttpServletRequest for authentication
     * @return ResponseEntity with created lobby
     */
    @PostMapping("/create")
    public ResponseEntity<?> createLobby(@Valid @RequestBody CreateLobbyDTO createLobbyDTO, HttpServletRequest request) {
        try {
            // Extract user ID from authentication token
            String hostId = authUtils.getUserIdFromToken(request);
            
            // Create lobby using service
            Lobby newLobby = lobbyService.createLobby(
                hostId, 
                createLobbyDTO.getNumberOfRounds(), 
                createLobbyDTO.getTimeLimit(), 
                createLobbyDTO.getMaxPlayers(), 
                createLobbyDTO.getGameSettings()
            );
            
            // Convert to DTO for response
            LobbyDTO lobbyDTO = new LobbyDTO(newLobby);
            lobbyDTO.setHost(true);
            lobbyDTO.setPlayerInLobby(true);
            
            // Fetch user info for response
            Map<String, Map<String, Object>> userInfoMap = getUserInfoForLobby(newLobby);
            
            // Create response DTO
            LobbyResponseDTO responseDTO = LobbyResponseDTO.fromLobbyDTO(lobbyDTO, userInfoMap, hostId);
            responseDTO.setPrivate(createLobbyDTO.getIsPrivate());
            responseDTO.setDifficulty(createLobbyDTO.getDifficulty());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error creating lobby", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating lobby", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error creating lobby: " + e.getMessage()));
        }
    }
    
    /**
     * Get lobby by code
     * @param lobbyCode The lobby code
     * @param request HttpServletRequest for authentication
     * @return ResponseEntity with lobby data
     */
    @GetMapping("/{lobbyCode}")
    public ResponseEntity<?> getLobbyByCode(@PathVariable String lobbyCode, HttpServletRequest request) {
        try {
            // Get current user ID
            String currentUserId = authUtils.getUserIdFromToken(request);
            
            // Find lobby
            Optional<Lobby> optionalLobby = lobbyService.findByLobbyCode(lobbyCode);
            if (optionalLobby.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Lobby not found"));
            }
            
            Lobby lobby = optionalLobby.get();
            
            // Convert to DTO
            LobbyDTO lobbyDTO = new LobbyDTO(lobby);
            lobbyDTO.setHost(currentUserId.equals(lobby.getHostId()));
            lobbyDTO.setPlayerInLobby(lobby.getPlayerIds().contains(currentUserId));
            
            // Fetch user info for all players
            Map<String, Map<String, Object>> userInfoMap = getUserInfoForLobby(lobby);
            
            // Create response DTO
            LobbyResponseDTO responseDTO = LobbyResponseDTO.fromLobbyDTO(lobbyDTO, userInfoMap, currentUserId);

            System.out.println("LobbyController.getLobbyByCode: lobbyDTO = " + responseDTO);
            
            return ResponseEntity.ok(responseDTO);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error getting lobby", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching lobby", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error fetching lobby: " + e.getMessage()));
        }
    }
    
    /**
     * Get lobbies for the authenticated user (as host)
     * @param request HttpServletRequest for authentication
     * @return ResponseEntity with list of lobbies
     */
    @GetMapping("/host")
    public ResponseEntity<?> getHostLobbies(HttpServletRequest request) {
        try {
            String hostId = authUtils.getUserIdFromToken(request);
            List<Lobby> lobbies = lobbyService.findLobbiesByHost(hostId);
            
            // Convert to DTO list
            List<LobbyDTO> lobbyDTOs = lobbies.stream()
                .map(lobby -> {
                    LobbyDTO dto = new LobbyDTO(lobby);
                    dto.setHost(true);
                    dto.setPlayerInLobby(true);
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(lobbyDTOs);
            
        } catch (IllegalArgumentException e) {
            logger.error("Authentication error", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching host lobbies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error fetching lobbies: " + e.getMessage()));
        }
    }
    
    /**
     * Get lobbies for the authenticated user (as player)
     * @param request HttpServletRequest for authentication
     * @return ResponseEntity with list of lobbies
     */
    @GetMapping("/player")
    public ResponseEntity<?> getPlayerLobbies(HttpServletRequest request) {
        try {
            String playerId = authUtils.getUserIdFromToken(request);
            List<Lobby> lobbies = lobbyService.findLobbiesByPlayer(playerId);
            
            // Convert to DTO list
            List<LobbyDTO> lobbyDTOs = lobbies.stream()
                .map(lobby -> {
                    LobbyDTO dto = new LobbyDTO(lobby);
                    dto.setHost(playerId.equals(lobby.getHostId()));
                    dto.setPlayerInLobby(true);
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(lobbyDTOs);} catch (IllegalArgumentException e) {
                logger.error("Authentication error", e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                logger.error("Error fetching player lobbies", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching lobbies: " + e.getMessage()));
            }
        }
        
        /**
         * Join a lobby
         * @param joinLobbyDTO DTO containing join data
         * @param request HttpServletRequest for authentication
         * @return ResponseEntity with updated lobby
         */
        @PostMapping("/join")
        public ResponseEntity<?> joinLobby(@Valid @RequestBody JoinLobbyDTO joinLobbyDTO, HttpServletRequest request) {
            try {
                String playerId = authUtils.getUserIdFromToken(request);
                String lobbyCode = joinLobbyDTO.getLobbyCode();
                
                Lobby updatedLobby = lobbyService.joinLobby(lobbyCode, playerId);
                
                // Convert to DTO
                LobbyDTO lobbyDTO = new LobbyDTO(updatedLobby);
                lobbyDTO.setHost(playerId.equals(updatedLobby.getHostId()));
                lobbyDTO.setPlayerInLobby(true);
                
                // Fetch user info for all players
                Map<String, Map<String, Object>> userInfoMap = getUserInfoForLobby(updatedLobby);
                
                // Create response DTO
                LobbyResponseDTO responseDTO = LobbyResponseDTO.fromLobbyDTO(lobbyDTO, userInfoMap, playerId);
                
                return ResponseEntity.ok(responseDTO);
                
            } catch (IllegalArgumentException e) {
                logger.error("Error joining lobby", e);
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                logger.error("Unexpected error joining lobby", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error joining lobby: " + e.getMessage()));
            }
        }
        
        /**
         * Leave a lobby
         * @param leaveLobbyDTO DTO containing leave data
         * @param request HttpServletRequest for authentication
         * @return ResponseEntity with status or updated lobby
         */
        @PostMapping("/leave")
        public ResponseEntity<?> leaveLobby(@Valid @RequestBody JoinLobbyDTO leaveLobbyDTO, HttpServletRequest request) {
            try {
                String playerId = authUtils.getUserIdFromToken(request);
                String lobbyCode = leaveLobbyDTO.getLobbyCode();
                
                Lobby updatedLobby = lobbyService.leaveLobby(lobbyCode, playerId);
                if (updatedLobby == null) {
                    // Lobby was closed (host left)
                    return ResponseEntity.ok(Map.of(
                        "status", "closed",
                        "message", "Lobby closed because host left"
                    ));
                } else {
                    // Convert to DTO
                    LobbyDTO lobbyDTO = new LobbyDTO(updatedLobby);
                    lobbyDTO.setHost(false);
                    lobbyDTO.setPlayerInLobby(false);
                    
                    // Fetch user info for all players
                    Map<String, Map<String, Object>> userInfoMap = getUserInfoForLobby(updatedLobby);
                    
                    // Create response DTO
                    LobbyResponseDTO responseDTO = LobbyResponseDTO.fromLobbyDTO(lobbyDTO, userInfoMap, playerId);
                    
                    return ResponseEntity.ok(responseDTO);
                }
                
            } catch (IllegalArgumentException e) {
                logger.error("Error leaving lobby", e);
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                logger.error("Unexpected error leaving lobby", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error leaving lobby: " + e.getMessage()));
            }
        }
        
        /**
         * Start the game
         * @param startGameDTO DTO containing start data
         * @param request HttpServletRequest for authentication
         * @return ResponseEntity with updated lobby
         */
        @PostMapping("/start")
        public ResponseEntity<?> startGame(@Valid @RequestBody JoinLobbyDTO startGameDTO, HttpServletRequest request) {
            try {
                String hostId = authUtils.getUserIdFromToken(request);
                String lobbyCode = startGameDTO.getLobbyCode();
                
                Lobby updatedLobby = lobbyService.startGame(lobbyCode, hostId);
                
                // Convert to DTO
                LobbyDTO lobbyDTO = new LobbyDTO(updatedLobby);
                lobbyDTO.setHost(true);
                lobbyDTO.setPlayerInLobby(true);
                
                // Fetch user info for all players
                Map<String, Map<String, Object>> userInfoMap = getUserInfoForLobby(updatedLobby);
                
                // Create response DTO
                LobbyResponseDTO responseDTO = LobbyResponseDTO.fromLobbyDTO(lobbyDTO, userInfoMap, hostId);
                
                return ResponseEntity.ok(responseDTO);
                
            } catch (IllegalArgumentException e) {
                logger.error("Error starting game", e);
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                logger.error("Unexpected error starting game", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error starting game: " + e.getMessage()));
            }
        }
        
        /**
         * End the game
         * @param endGameDTO DTO containing end data
         * @param request HttpServletRequest for authentication
         * @return ResponseEntity with updated lobby
         */
        @PostMapping("/end")
        public ResponseEntity<?> endGame(@Valid @RequestBody JoinLobbyDTO endGameDTO, HttpServletRequest request) {
            try {
                // Verify authentication (even though we don't use the user ID directly)
                String userId = authUtils.getUserIdFromToken(request);
                
                String lobbyCode = endGameDTO.getLobbyCode();
                
                Lobby updatedLobby = lobbyService.endGame(lobbyCode);
                
                // Convert to DTO
                LobbyDTO lobbyDTO = new LobbyDTO(updatedLobby);
                lobbyDTO.setHost(userId.equals(updatedLobby.getHostId()));
                lobbyDTO.setPlayerInLobby(updatedLobby.getPlayerIds().contains(userId));
                
                // Fetch user info for all players
                Map<String, Map<String, Object>> userInfoMap = getUserInfoForLobby(updatedLobby);
                
                // Create response DTO
                LobbyResponseDTO responseDTO = LobbyResponseDTO.fromLobbyDTO(lobbyDTO, userInfoMap, userId);
                
                return ResponseEntity.ok(responseDTO);
                
            } catch (IllegalArgumentException e) {
                logger.error("Error ending game", e);
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                logger.error("Unexpected error ending game", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error ending game: " + e.getMessage()));
            }
        }
        
        /**
         * Close a lobby
         * @param closeLobbyDTO DTO containing close data
         * @param request HttpServletRequest for authentication
         * @return ResponseEntity with status
         */
        @PostMapping("/close")
        public ResponseEntity<?> closeLobby(@Valid @RequestBody JoinLobbyDTO closeLobbyDTO, HttpServletRequest request) {
            try {
                // Verify authentication
                String userId = authUtils.getUserIdFromToken(request);
                
                String lobbyCode = closeLobbyDTO.getLobbyCode();
                
                // Verify user is the host before closing
                Optional<Lobby> optionalLobby = lobbyService.findByLobbyCode(lobbyCode);
                if (optionalLobby.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Lobby not found"));
                }
                
                Lobby lobby = optionalLobby.get();
                if (!userId.equals(lobby.getHostId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only the host can close the lobby"));
                }
                
                lobbyService.closeLobby(lobbyCode);
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Lobby closed successfully"
                ));
                
            } catch (IllegalArgumentException e) {
                logger.error("Error closing lobby", e);
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                logger.error("Unexpected error closing lobby", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error closing lobby: " + e.getMessage()));
            }
        }
        
        /**
         * Update lobby settings
         * @param lobbyCode The lobby code
         * @param settingsDTO DTO containing updated settings
         * @param request HttpServletRequest for authentication
         * @return ResponseEntity with updated lobby
         */
        @PutMapping("/{lobbyCode}/settings")
        public ResponseEntity<?> updateLobbySettings(
                @PathVariable String lobbyCode, 
                @Valid @RequestBody LobbySettingsDTO settingsDTO,
                HttpServletRequest request) {
            try {
                String hostId = authUtils.getUserIdFromToken(request);
                
                Lobby updatedLobby = lobbyService.updateLobbySettings(
                    lobbyCode, 
                    hostId, 
                    settingsDTO.getNumberOfRounds(), 
                    settingsDTO.getTimeLimit(), 
                    settingsDTO.getMaxPlayers(), 
                    settingsDTO.getGameSettings()
                );
                
                // Convert to DTO
                LobbyDTO lobbyDTO = new LobbyDTO(updatedLobby);
                lobbyDTO.setHost(true);
                lobbyDTO.setPlayerInLobby(true);
                
                // Fetch user info for all players
                Map<String, Map<String, Object>> userInfoMap = getUserInfoForLobby(updatedLobby);
                
                // Create response DTO
                LobbyResponseDTO responseDTO = LobbyResponseDTO.fromLobbyDTO(lobbyDTO, userInfoMap, hostId);
                
                // Set additional properties if provided
                if (settingsDTO.getIsPrivate() != null) {
                    responseDTO.setPrivate(settingsDTO.getIsPrivate());
                }
                
                if (settingsDTO.getDifficulty() != null) {
                    responseDTO.setDifficulty(settingsDTO.getDifficulty());
                }
                
                return ResponseEntity.ok(responseDTO);
                
            } catch (IllegalArgumentException e) {
                logger.error("Error updating lobby settings", e);
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                logger.error("Unexpected error updating lobby settings", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error updating settings: " + e.getMessage()));
            }
        }
        
        /**
         * Get active lobbies count
         * @return ResponseEntity with count
         */
        @GetMapping("/active/count")
        public ResponseEntity<?> getActiveLobbiesCount() {
            try {
                long count = lobbyService.getActiveLobbiesCount();
                
                return ResponseEntity.ok(Map.of("count", count));
                
            } catch (Exception e) {
                logger.error("Error getting active lobbies count", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error getting count: " + e.getMessage()));
            }
        }
        
        /**
         * Helper method to fetch user information for all players in a lobby
         * @param lobby The lobby
         * @return Map of user IDs to user info maps
         */
        private Map<String, Map<String, Object>> getUserInfoForLobby(Lobby lobby) {
            Map<String, Map<String, Object>> userInfoMap = new HashMap<>();
            
            for (String playerId : lobby.getPlayerIds()) {
                try {
                    Optional<User> userOpt = userService.findByClerkUserId(playerId);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("id", user.getId());
                        userInfo.put("username", user.getUsername());
                        userInfo.put("displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
                        userInfo.put("profilePicture", user.getProfilePicture());
                        
                        userInfoMap.put(playerId, userInfo);
                    }
                } catch (Exception e) {
                    logger.warn("Error fetching user info for player {}: {}", playerId, e.getMessage());
                }
            }
            
            return userInfoMap;
        }
    }