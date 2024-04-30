package feup.cpd.server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class MessageHandler implements Callable<Void> {
    static ExecutorService executorService;
    final SocketChannel socketChannel;

    public MessageHandler(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public static void handleMessage(SocketChannel socketChannel){

    }

    @Override
    public Void call() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024); //1kb buffer, messages should not be larger than that
        try{
            socketChannel.read(byteBuffer);
            //TODO(luisd): do something with message
            System.out.printf("Received message from %s: %s",
                    socketChannel.getRemoteAddress(), new String(byteBuffer.array()));
            executorService.submit(new MessageHandler(socketChannel));
        } catch (Exception e){
            //throw execption at runtime
            throw new RuntimeException(e);
        }
        return null;
    }
}
