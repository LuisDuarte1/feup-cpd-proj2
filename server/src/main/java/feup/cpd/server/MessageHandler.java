package feup.cpd.server;

import feup.cpd.protocol.MessageReader;
import feup.cpd.protocol.ProtocolFacade;
import feup.cpd.protocol.models.LoginRequest;
import feup.cpd.protocol.models.ProtocolModel;
import feup.cpd.protocol.models.QueueJoin;
import feup.cpd.protocol.models.QueueToken;
import feup.cpd.server.concurrent.ConcurrentSocketChannel;
import feup.cpd.server.concurrent.helper.LockedValue;
import feup.cpd.server.models.PlayerState;
import feup.cpd.server.services.LoginService;
import feup.cpd.server.services.QueueService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static feup.cpd.protocol.MessageReader.INITIAL_BUFFER_SIZE;

@SuppressWarnings("ALL")
public class MessageHandler implements Callable<Void> {
    static ExecutorService executorService;
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
            ProtocolModel protocolModel = MessageReader.readMessageFromSocket(socketChannel.socketChannel);
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
                case null -> null;
                default -> throw new IllegalStateException(
                        "Unexpected value: " +
                                protocolModel.getClass().getName());
            };

            if (response == null) return null;

            socketChannel.writeLock.lock();
            try {
                socketChannel.socketChannel.write(response);
            } finally {
                socketChannel.writeLock.unlock();
            }

            executorService.submit(new MessageHandler(socketChannel, playerStateLockedValue));

            return null;
        } catch (RuntimeException | IOException e){
            System.err.println("Throwable: " + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            socketChannel.readLock.unlock();
        }
    }
}
