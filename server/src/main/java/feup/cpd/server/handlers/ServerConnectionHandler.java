package feup.cpd.server.handlers;

import feup.cpd.server.App;
import feup.cpd.server.concurrent.ConcurrentSocketChannel;
import feup.cpd.server.handlers.MessageHandler;
import feup.cpd.server.models.PlayerState;

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
        System.out.println("Ready to accept connections on :4206");
        while (true){
            try {
                final SocketChannel socketChannel = serverSocketChannel.accept();
                System.out.printf("Accepting connection from %s\n", socketChannel.getRemoteAddress());
                var concurrentSocketChannel = new ConcurrentSocketChannel(socketChannel);
                var lockedState = App.connectedPlayersState.put(concurrentSocketChannel, PlayerState.UNAUTHENTICATED);
                executorService.submit(new MessageHandler(concurrentSocketChannel, lockedState));
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        }

    }

    public void handleConnections() throws ExecutionException, InterruptedException{
        var future = executorService.submit(this::handler);
        future.get();
    }
}
