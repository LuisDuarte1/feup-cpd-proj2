package feup.cpd.server.services;

import feup.cpd.protocol.ProtocolFacade;
import feup.cpd.protocol.models.QueueJoin;
import feup.cpd.protocol.models.QueueToken;
import feup.cpd.protocol.models.Status;
import feup.cpd.protocol.models.enums.ProtocolType;
import feup.cpd.protocol.models.enums.StatusType;
import feup.cpd.server.App;
import feup.cpd.server.concurrent.ConcurrentSocketChannel;
import feup.cpd.server.concurrent.helper.LockedValue;
import feup.cpd.server.models.PlayerState;
import feup.cpd.server.repositories.NormalQueueRepository;

import java.nio.ByteBuffer;
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


        if(queueJoin.type == ProtocolType.QUEUE_JOIN){

            var normalQueueRepo = NormalQueueRepository.getInstance(executorService);

            String name = App.playersLoggedOn.get(concurrentSocketChannel, String::new);

            if(normalQueueRepo.checkIfUserInQueue(name)){
                return ProtocolFacade.createPacket(
                        new Status(StatusType.INVALID_REQUEST, "Can't add user that's already on queue")
                );
            }

            UUID uuid = normalQueueRepo.addToQueue(name);

            boolean canCreateGame = normalQueueRepo.checkIfGameCanBeStarted(App.PLAYER_GAME_COUNT);

            if(canCreateGame){
                var gameCandidates = normalQueueRepo.getGameCandidates(App.PLAYER_GAME_COUNT);
                //TODO: do something with them
            }

            return ProtocolFacade.createPacket(new QueueToken(uuid));
        }



        throw new RuntimeException("Didn't implement ranked queues yet.");
    }
}
