package feup.cpd.server.services;

import feup.cpd.protocol.ProtocolFacade;
import feup.cpd.protocol.models.*;
import feup.cpd.protocol.models.enums.ProtocolType;
import feup.cpd.protocol.models.enums.QueueType;
import feup.cpd.protocol.models.enums.StatusType;
import feup.cpd.server.App;
import feup.cpd.server.concurrent.ConcurrentSocketChannel;
import feup.cpd.server.concurrent.helper.LockedValue;
import feup.cpd.server.handlers.CreateGameHandler;
import feup.cpd.server.handlers.GameFoundHandler;
import feup.cpd.server.handlers.GameFoundTimeoutHandler;
import feup.cpd.server.models.Player;
import feup.cpd.server.models.PlayerState;
import feup.cpd.server.repositories.NormalQueueRepository;
import feup.cpd.server.repositories.PlayerRepository;
import feup.cpd.server.repositories.RankedQueueRepository;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class QueueService {

    public static ByteBuffer handleQueueJoinRequest(
            LockedValue<PlayerState> playerState, QueueJoin queueJoin, ExecutorService executorService,
            ConcurrentSocketChannel concurrentSocketChannel){
        playerState.reentrantLock.lock();
        try {
            if(playerState.value != PlayerState.LOGGED_IN)
                return ProtocolFacade.createPacket(
                        new Status(StatusType.INVALID_REQUEST, "User must be logged in and not " +
                                "in a game in order to join a queue."));
        } finally {
            playerState.reentrantLock.unlock();
        }

        String name = App.playersLoggedOn.lockAndRead((map) -> map.get(concurrentSocketChannel));

        if(queueJoin.queueType == QueueType.NORMAL){

            var normalQueueRepo = NormalQueueRepository.getInstance(executorService);


            if(normalQueueRepo.checkIfUserInQueue(name)){
                return ProtocolFacade.createPacket(
                        new Status(StatusType.INVALID_REQUEST, "Can't add user that's already on queue")
                );
            }

            UUID uuid = normalQueueRepo.addToQueue(name);

            playerState.reentrantLock.lock();
            try {
                playerState.value = PlayerState.NORMAL_QUEUE;
            } finally {
                playerState.reentrantLock.unlock();
            }

            System.out.printf("Added %s to queue\n", name);
            boolean canCreateGame = normalQueueRepo.checkIfGameCanBeStarted(App.PLAYER_GAME_COUNT);
            if(!canCreateGame){
                return ProtocolFacade.createPacket(new QueueToken(uuid));
            }

            System.out.println("Sufficient players to create normal game: adding handler to queue");
            var gameCandidates = normalQueueRepo.getGameCandidates(App.PLAYER_GAME_COUNT);
            executorService.submit(new GameFoundHandler(gameCandidates, QueueType.NORMAL));
            return ProtocolFacade.createPacket(new QueueToken(uuid));


        }

        var rankedQueueRepo = RankedQueueRepository.getInstance(executorService);
        var playerRepo = PlayerRepository.getInstance(executorService);

        if(rankedQueueRepo.checkIfUserInQueue(name)){
            return ProtocolFacade.createPacket(
                    new Status(StatusType.INVALID_REQUEST, "Can't add user that's already on queue")
            );
        }

        final int elo = playerRepo.getFromPlayer(name, Player::getElo);

        UUID uuid = rankedQueueRepo.addToQueue(name, elo);

        playerState.reentrantLock.lock();
        try {
            playerState.value = PlayerState.RANKED_QUEUE;
        } finally {
            playerState.reentrantLock.unlock();
        }

        System.out.printf("Added %s to ranked queue\n", name);
        //no need to handle game join, as it will be handled eventually by RankedQueueMatchmakerHandler
        return ProtocolFacade.createPacket(new QueueToken(uuid));

    }

    public static ByteBuffer handleAcceptQueue(LockedValue<PlayerState> playerState, AcceptMatch acceptMatch, ExecutorService executorService,
                                               ConcurrentSocketChannel concurrentSocketChannel){

        playerState.reentrantLock.lock();
        try {
            if(playerState.value != PlayerState.FOUND_GAME){
                return ProtocolFacade.createPacket(
                        new Status(StatusType.NOT_IN_QUEUE, "You are not in a accepting game state anymore.. please try again.")
                );
            }
        } finally {
            playerState.reentrantLock.unlock();
        }
        var matchPair = App.pendingMatches.get(acceptMatch.matchId);
        if(matchPair == null){
            return ProtocolFacade.createPacket(
                    new Status(StatusType.OK, "Someone else didn't accept the game... returning to queue.")
            );
        }
        QueueType queueType = matchPair.value.third();

        var playerName = App.playersLoggedOn.lockAndRead(((val) -> val.get(concurrentSocketChannel)));

        if(!acceptMatch.acceptMatchBoolean){
            GameFoundTimeoutHandler.removePendingMatch(acceptMatch.matchId,
                    playerName,
                    queueType);
            return ProtocolFacade.createPacket(
                    new Status(StatusType.OK, "Removed from found game and didn't return to queue"));
        }


        matchPair.reentrantLock.lock();
        try {
            //check if match still exists inside lock
            if(App.pendingMatches.get(acceptMatch.matchId) == null){
                return ProtocolFacade.createPacket(
                        new Status(StatusType.OK, "Someone else didn't accept the game... returning to queue.")
                );
            }
            matchPair.value.second().remove(playerName);
            matchPair.value.second().put(playerName, true);

            var everyoneAccepted = matchPair.value.second().values().stream().reduce((a,b) -> a && b).get();

            if(everyoneAccepted){
                System.out.printf("Game %s has been accepted by all... starting game.\n", acceptMatch.matchId);
                final Status gameStarting = new Status(StatusType.MATCH_STARTING, "Everyone accepted... match starting");
                for(var entry : matchPair.value.first().entrySet()){
                    if(Objects.equals(entry.getKey(), playerName)) continue;
                    var connection = App.playersLoggedOn.lockAndRead((val) -> val.getInverse(entry.getKey()));
                    connection.writeLock.lock();
                    try {
                        connection.socketChannel.write(ProtocolFacade.createPacket(gameStarting));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        connection.writeLock.unlock();
                    }
                }
                executorService.submit(new CreateGameHandler(
                        matchPair.value.first().keySet().stream().toList(),
                        acceptMatch.matchId, matchPair.value.third()));
                return ProtocolFacade.createPacket(gameStarting);
            }

            return ProtocolFacade.createPacket(
                    new Status(StatusType.OK, "Accepted game... waiting for other players"));
        } finally {
            matchPair.reentrantLock.unlock();
        }

    }
}
