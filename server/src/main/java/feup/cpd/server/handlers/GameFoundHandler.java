package feup.cpd.server.handlers;

import feup.cpd.protocol.ProtocolFacade;
import feup.cpd.protocol.models.MatchFound;
import feup.cpd.protocol.models.enums.QueueType;
import feup.cpd.server.App;
import feup.cpd.server.collections.Pair;
import feup.cpd.server.collections.Triple;
import feup.cpd.server.concurrent.ConcurrentSocketChannel;
import feup.cpd.server.exceptions.PlayerLeftQueueException;
import feup.cpd.server.models.PlayerState;
import feup.cpd.server.repositories.NormalQueueRepository;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class GameFoundHandler implements Callable<Void> {

    public static ExecutorService executorService;

    final Map<String, UUID> gameCandidates;

    final QueueType queueType;

    public GameFoundHandler(Map<String, UUID> gameCandidates, QueueType queueType) {
        this.gameCandidates = gameCandidates;
        this.queueType = queueType;
    }

    @Override
    public Void call() throws Exception {
        Map<String, ConcurrentSocketChannel> socketChannelMap = new HashMap<>();
        try {
            for (var entry : gameCandidates.entrySet()) {
                socketChannelMap.put(
                        entry.getKey(),
                        App.playersLoggedOn.lockAndRead((map) -> map.getInverse(entry.getKey()))
                );
                var lockedPlayerState = App.connectedPlayersState.get(socketChannelMap.get(entry.getKey()));
                lockedPlayerState.reentrantLock.lock();
                try {
                    //meanwhile the player left the associated queue, so we disband the match
                    if (
                            (queueType == QueueType.NORMAL && lockedPlayerState.value != PlayerState.NORMAL_QUEUE) ||
                                    (queueType == QueueType.RANKED && lockedPlayerState.value != PlayerState.RANKED_QUEUE)
                    ) {
                        throw new PlayerLeftQueueException(entry.getKey());
                    }
                    lockedPlayerState.value = PlayerState.FOUND_GAME;
                } finally {
                    lockedPlayerState.reentrantLock.unlock();
                }
            }

            // now we create pending match
            UUID matchUUID = UUID.randomUUID();
            Map<String, Boolean> acceptedMap = new HashMap<>();
            gameCandidates.forEach((k,v) -> acceptedMap.put(k, false));
            App.pendingMatches.put(matchUUID, new Triple<>(gameCandidates, acceptedMap, QueueType.NORMAL));

            //now we send the match id to clients, to see if they accept the game
            for(var entry: socketChannelMap.entrySet()){
                entry.getValue().writeLock.lock();
                try {
                    entry.getValue().socketChannel.write(
                            ProtocolFacade.createPacket(new MatchFound(matchUUID))
                    );
                } finally {
                    entry.getValue().writeLock.unlock();
                }
                executorService.submit(new GameFoundTimeoutHandler(matchUUID, entry.getKey(), queueType));
            }




        } catch (PlayerLeftQueueException playerLeftQueueException){
            gameCandidates.remove(playerLeftQueueException.playerName);
            // we restore the game state
            for (var entry : gameCandidates.entrySet()) {
                var lockedPlayerState = App.connectedPlayersState.get(socketChannelMap.get(entry.getKey()));
                // check if null because the sudden disconnection might be already handled.
                if(lockedPlayerState == null){
                    continue;
                }
                lockedPlayerState.reentrantLock.lock();
                try{
                    lockedPlayerState.value = queueType == QueueType.NORMAL ? PlayerState.NORMAL_QUEUE
                            : PlayerState.RANKED_QUEUE;
                } finally {
                    lockedPlayerState.reentrantLock.unlock();
                }
            }
            if(queueType == QueueType.NORMAL){
                NormalQueueRepository.getInstance(executorService).reAddGameCandidates(gameCandidates);
            }
            //TODO(luisd): should we inform users that they are back on queue?
        }
        return null;
    }
}
