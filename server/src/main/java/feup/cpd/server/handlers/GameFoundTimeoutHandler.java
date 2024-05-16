package feup.cpd.server.handlers;

import feup.cpd.protocol.models.enums.QueueType;
import feup.cpd.server.App;
import feup.cpd.server.models.PlayerState;
import feup.cpd.server.repositories.NormalQueueRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class GameFoundTimeoutHandler implements Callable<Void> {

    public static ExecutorService executorService;

    static final int GAME_ACCEPT_TIMEOUT = 20;

    final UUID matchUUID;

    final String playerName;

    final QueueType queueType;

    public GameFoundTimeoutHandler(UUID matchUUID, String playerName, QueueType queueType) {
        this.matchUUID = matchUUID;
        this.playerName = playerName;
        this.queueType = queueType;
    }


    @Override
    public Void call() throws Exception {
        Thread.sleep(GAME_ACCEPT_TIMEOUT*1000);
        removePendingMatch(matchUUID, playerName, queueType);

        return null;
    }

    public static void removePendingMatch(UUID matchUUID, String playerName, QueueType queueType) {
        var pendingTuple = App.pendingMatches.get(matchUUID);
        //means that game doesn't exist anymore, and we should return immediately because it has already been handled
        if(pendingTuple == null) return;
        var accepted = false;
        pendingTuple.reentrantLock.lock();
        try {
            accepted = pendingTuple.value.second().get(playerName);
        } finally {
            pendingTuple.reentrantLock.unlock();
        }
        if(accepted) return;

        //now we return all players, except this one to the queue
        pendingTuple.reentrantLock.lock();
        try {
            // check if match still exists,
            // because another player might have cancelled it before locking.
            if(App.pendingMatches.get(matchUUID) == null) return;
            System.out.printf("Removing pending match %s due to player %s\n", matchUUID, playerName);
            pendingTuple.value.first().remove(playerName);
            var playerConnection = App.playersLoggedOn.lockAndRead((map) -> map.getInverse(playerName));
            var playerState = App.connectedPlayersState.get(playerConnection);
            if(playerState != null){
                playerState.reentrantLock.lock();
                try {
                    //remove him from queue entirely
                    playerState.value = PlayerState.LOGGED_IN;
                } finally {
                    playerState.reentrantLock.unlock();
                }
            }
            List<String> toRemove = new ArrayList<>(pendingTuple.value.first().size());
            for(var entry: pendingTuple.value.first().entrySet()){


                var connection = App.playersLoggedOn.lockAndRead((map) -> map.getInverse(entry.getKey()));
                //client might have disconnected then, so we remove it from the queue also.
                if(connection == null){
                    toRemove.add(entry.getKey());
                    continue;
                }
                //restore playerState
                var lockedPlayerState = App.connectedPlayersState.get(connection);
                lockedPlayerState.reentrantLock.lock();
                try {
                    lockedPlayerState.value = queueType == QueueType.NORMAL ? PlayerState.NORMAL_QUEUE
                            : PlayerState.RANKED_QUEUE;
                } finally {
                    lockedPlayerState.reentrantLock.unlock();
                }
            }

            toRemove.forEach((val) -> pendingTuple.value.first().remove(val));

            if(queueType == QueueType.NORMAL){
                NormalQueueRepository.getInstance(executorService).reAddGameCandidates(pendingTuple.value.first());
            } else{
                throw new RuntimeException("Ranked queue accept timeouts are not implemented.");
            }
            App.pendingMatches.delete(matchUUID);
        } finally {
            pendingTuple.reentrantLock.unlock();
        }
    }
}
