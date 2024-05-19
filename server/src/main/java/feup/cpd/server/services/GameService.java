package feup.cpd.server.services;

import feup.cpd.game.Card;
import feup.cpd.game.Game;
import feup.cpd.protocol.ProtocolFacade;
import feup.cpd.protocol.models.CardPlayed;
import feup.cpd.protocol.models.GameState;
import feup.cpd.protocol.models.Status;
import feup.cpd.protocol.models.enums.QueueType;
import feup.cpd.protocol.models.enums.StatusType;
import feup.cpd.server.App;
import feup.cpd.server.concurrent.ConcurrentSocketChannel;
import feup.cpd.server.concurrent.helper.LockedValue;
import feup.cpd.server.models.Player;
import feup.cpd.server.models.PlayerState;
import feup.cpd.server.repositories.PlayerRepository;
import feup.cpd.server.repositories.RankedQueueRepository;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class GameService {

    public static ByteBuffer handleCardPlayed(LockedValue<PlayerState> playerState,
                                              CardPlayed cardPlayed, ExecutorService executorService,
                                              ConcurrentSocketChannel concurrentSocketChannel){
        playerState.reentrantLock.lock();
        try{
            if(playerState.value != PlayerState.IN_GAME){
                return ProtocolFacade.createPacket(
                    new Status(StatusType.INVALID_REQUEST, "User not in game anymore..")
                );
            }
        }finally {
            playerState.reentrantLock.unlock();
        }
        var match = App.activeGames.get(cardPlayed.matchUUID);
        var playerName = App.playersLoggedOn.lockAndRead((map)-> map.get(concurrentSocketChannel));
        if(match == null){
            return ProtocolFacade.createPacket(
                    new Status(StatusType.INVALID_REQUEST, "Match doesnt exist anymore..."));
        }

        match.reentrantLock.lock();
        try {
            var game = match.value;
            if(!game.checkIfPlayerBelongs(playerName)){
                return ProtocolFacade.createPacket(
                        new Status(StatusType.INVALID_REQUEST, "You are not in this game..."));
            }
            if(!Objects.equals(game.getCurrentPlayerName(), playerName)){
                return ProtocolFacade.createPacket(
                        new GameState(false, cardPlayed.matchUUID, game.getTopCard(), new ArrayList<>()));
            }
            if(!game.playCard(cardPlayed.card)){
                return ProtocolFacade.createPacket(
                        new GameState(true, cardPlayed.matchUUID, game.getTopCard(), game.getCurrentPlayerHand()));
            }

            if(game.isGameOver()){
                return handleWinCondition(playerState, game, playerName, executorService);
            }

            List<Card> drawnCards = new ArrayList<>();
            while (true){
                Optional<Card> optionalCard = game.checkAndDrawHeadless();
                if(optionalCard.isEmpty()) break;
                drawnCards.add(optionalCard.get());
            }
            while (!game.checkIfCanPlay()){
                System.out.printf("Skipping turn on match %s because there aren't enough cards...\n", cardPlayed.matchUUID);
                drawnCards.clear();
                game.nextTurn();
            }
            var newPlayerName = game.getCurrentPlayerName();
            for(var player: game.getPlayers()){
                if(Objects.equals(player.name, playerName)) continue;

                var playerConn = App.playersLoggedOn.lockAndRead((map) -> map.getInverse(player.name));
                playerConn.writeLock.lock();
                try {
                    playerConn.socketChannel.write(ProtocolFacade.createPacket(
                            new GameState(Objects.equals(player.name, newPlayerName), cardPlayed.matchUUID,
                                    game.getTopCard(),
                                    Objects.equals(player.name, newPlayerName) ? game.getCurrentPlayerHand() : new ArrayList<>(),
                                    Objects.equals(player.name, newPlayerName) ? drawnCards : List.of())
                    ));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    playerConn.writeLock.unlock();
                }
            }

            return ProtocolFacade.createPacket(
                    new GameState(Objects.equals(playerName, newPlayerName), cardPlayed.matchUUID,
                            game.getTopCard(),
                            Objects.equals(playerName, newPlayerName) ? game.getCurrentPlayerHand() : new ArrayList<>(),
                            Objects.equals(playerName, newPlayerName) ? drawnCards : List.of())
            );
        } finally {
            match.reentrantLock.unlock();
        }
    }

    private static ByteBuffer handleWinCondition(LockedValue<PlayerState> playerState, Game game, String playerName,
                                                 ExecutorService executorService) {
        if(game.getQueueType() == QueueType.RANKED){
            for(var player: game.getPlayers()){
                if (Objects.equals(player.name, playerName)){
                    var elo = PlayerRepository.getInstance(executorService).getFromPlayer(playerName, Player::getElo);
                    PlayerRepository.getInstance(executorService).mutatePlayer(playerName, (player1) -> {
                        player1.setElo(elo + 10);
                        return null;
                    });
                    continue;
                }
                var elo = PlayerRepository.getInstance(executorService).getFromPlayer(playerName, Player::getElo);
                PlayerRepository.getInstance(executorService).mutatePlayer(playerName, (player1) -> {
                    player1.setElo(elo - 10);
                    return null;
                });
            }
        }

        for(var player: game.getPlayers()) {
            if (Objects.equals(player.name, playerName)) continue;
            var playerConn = App.playersLoggedOn.lockAndRead((map) -> map.getInverse(player.name));
            var state = App.connectedPlayersState.get(playerConn);
            state.reentrantLock.lock();
            try {
                state.value = PlayerState.LOGGED_IN;
            } finally {
                state.reentrantLock.unlock();
            }

            playerConn.writeLock.lock();
            try {
                playerConn.socketChannel.write(ProtocolFacade.createPacket(
                        new Status(StatusType.GAME_OVER, "You lost. Returning to main room")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                playerConn.writeLock.unlock();
            }
        }


        playerState.reentrantLock.lock();
        try {
            playerState.value = PlayerState.LOGGED_IN;
        } finally {
            playerState.reentrantLock.unlock();
        }
        return ProtocolFacade.createPacket(
                new Status(StatusType.GAME_OVER, "You win!!! Returning to main room"));
    }
}
