package feup.cpd.server;

import feup.cpd.protocol.MessageReader;
import feup.cpd.protocol.ProtocolFacade;
import feup.cpd.protocol.models.LoginRequest;
import feup.cpd.protocol.models.ProtocolModel;
import feup.cpd.server.services.LoginService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static feup.cpd.protocol.MessageReader.INITIAL_BUFFER_SIZE;

@SuppressWarnings("ALL")
public class MessageHandler implements Callable<Void> {
    static ExecutorService executorService;
    final SocketChannel socketChannel;


    public MessageHandler(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public static void handleMessage(SocketChannel socketChannel){

    }

    @Override
    public Void call() {
        try {
            ProtocolModel protocolModel = MessageReader.readMessageFromSocket(socketChannel);
            ByteBuffer response = switch (protocolModel) {
                case LoginRequest loginRequest -> LoginService.handleLoginRequest(loginRequest, executorService);
                case null -> null;
                default -> throw new IllegalStateException(
                        "Unexpected value: " +
                                protocolModel.getClass().getName());
            };

            if (response == null) return null;

            socketChannel.write(response);


            executorService.submit(new MessageHandler(socketChannel));
            return null;
        } catch (RuntimeException | IOException e){
            System.err.println("Throwable: " + e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
