package com.gptuessr.ai_game.repository;

import com.gptuessr.ai_game.entity.Lobby;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LobbyRepository extends MongoRepository<Lobby, String> {
    Optional<Lobby> findByLobbyCode(String lobbyCode);
    
    List<Lobby> findByHostId(String hostId);
    
    List<Lobby> findByPlayerIdsContaining(String playerId);
    
    List<Lobby> findByStatus(Lobby.GameStatus status);
    
    List<Lobby> findByStatusAndCreatedAtBefore(Lobby.GameStatus status, LocalDateTime time);
    
    boolean existsByLobbyCode(String lobbyCode);
    
    long countByStatus(Lobby.GameStatus status);
}
