package feup.cpd.server.handlers;

import feup.cpd.protocol.MessageReader;
import feup.cpd.protocol.models.*;
import feup.cpd.server.App;
import feup.cpd.server.concurrent.ConcurrentSocketChannel;
import feup.cpd.server.concurrent.helper.LockedValue;
import feup.cpd.server.models.PlayerState;
import feup.cpd.server.services.GameService;
import feup.cpd.server.services.LoginService;
import feup.cpd.server.services.QueueService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

@SuppressWarnings("ALL")
public class MessageHandler implements Callable<Void> {
    public static ExecutorService executorService;
    final ConcurrentSocketChannel socketChannel;

    //we cache the locked value since it becomes easier to access it without potentially waiting for the Map
    final LockedValue<PlayerState> playerStateLockedValue;


    public MessageHandler(ConcurrentSocketChannel socketChannel, LockedValue<PlayerState> playerStateLockedValue) {
        this.socketChannel = socketChannel;
        this.playerStateLockedValue = playerStateLockedValue;
    }

    @Override
    public Void call() {
        socketChannel.readLock.lock();
        try {
            List<ProtocolModel> protocolModels = MessageReader.readMessageFromSocket(socketChannel.socketChannel,
                (Void unused)->{
                    System.out.println("Handling disconnection and removing player from state");
                    App.playersLoggedOn.lockAndWrite((map) -> {
                        map.remove(socketChannel);
                        return null;
                    });
                    App.connectedPlayersState.delete(socketChannel);
                return null;
            });
            protocolModels.forEach((protocolModel -> {
                ByteBuffer response = switch (protocolModel) {
                    case LoginRequest loginRequest ->
                            LoginService.handleLoginRequest(
                                    playerStateLockedValue, loginRequest,
                                    executorService, socketChannel
                            );
                    case QueueJoin queueJoin ->
                            QueueService.handleQueueJoinRequest(
                                    playerStateLockedValue, queueJoin,
                                    executorService, socketChannel
                            );
                    case AcceptMatch acceptMatch ->
                            QueueService.handleAcceptQueue(
                                    playerStateLockedValue, acceptMatch,
                                    executorService, socketChannel);
                    case CardPlayed cardPlayed -> GameService.handleCardPlayed(
                            playerStateLockedValue, cardPlayed,
                            executorService, socketChannel);
                    case null -> null;
                    default -> throw new IllegalStateException(
                            "Unexpected value: " +
                                    protocolModel.getClass().getName());
                };

                if (response != null){
                    socketChannel.writeLock.lock();
                    try {
                        socketChannel.socketChannel.write(response);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        socketChannel.writeLock.unlock();
                    }
                }
            }));

            executorService.submit(new MessageHandler(socketChannel, playerStateLockedValue));

            return null;
        } catch (RuntimeException e){
            System.err.println("Throwable: " + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            socketChannel.readLock.unlock();
        }
    }
}
