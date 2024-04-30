package feup.cpd.server;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class ServerConnectionHandler {
    final ServerSocketChannel serverSocketChannel;
    final ExecutorService executorService;

    public ServerConnectionHandler(ServerSocketChannel serverSocketChannel, ExecutorService executorService) {
        this.serverSocketChannel = serverSocketChannel;
        this.executorService = executorService;
    }

    private void handler(){
        while (true){
            try {
                final SocketChannel socketChannel = serverSocketChannel.accept();
                System.out.printf("Accepting connection from %s\n", socketChannel.getRemoteAddress());
                executorService.submit(new MessageHandler(socketChannel));
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        }

    }

    public void handleConnections() throws ExecutionException, InterruptedException {
        var future = executorService.submit(this::handler);
        future.get();
    }
}
