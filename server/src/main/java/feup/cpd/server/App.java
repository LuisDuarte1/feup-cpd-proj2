/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package feup.cpd.server;


import feup.cpd.server.concurrent.ConcurrentRWMap;
import feup.cpd.server.models.Player;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {

    public static ConcurrentRWMap<SocketChannel, Player> connectedPlayers;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(4206));
        final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        final ServerConnectionHandler serverConnectionHandler = new ServerConnectionHandler(
                serverSocketChannel, executorService
        );
        //propagate executorService to all handlers
        MessageHandler.executorService = executorService;

        serverConnectionHandler.handleConnections();

        //if it reaches here, we can shutdown all tasks without executing them.
        executorService.shutdownNow();

    }
}
