/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package feup.cpd.server;


import feup.cpd.protocol.models.enums.QueueType;
import feup.cpd.server.collections.BidirectionalUniqueMap;
import feup.cpd.server.collections.Pair;
import feup.cpd.server.collections.Triple;
import feup.cpd.server.concurrent.ConcurrentExclusiveMap;
import feup.cpd.server.concurrent.ConcurrentSocketChannel;
import feup.cpd.server.concurrent.helper.RWLockedValue;
import feup.cpd.server.handlers.GameFoundHandler;
import feup.cpd.server.handlers.GameFoundTimeoutHandler;
import feup.cpd.server.handlers.MessageHandler;
import feup.cpd.server.handlers.ServerConnectionHandler;
import feup.cpd.server.models.PlayerState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.Map;
import java.util.concurrent.Executors;

public class App {

    public static ConcurrentExclusiveMap<ConcurrentSocketChannel, PlayerState> connectedPlayersState =
            new ConcurrentExclusiveMap<>();

    public static RWLockedValue<BidirectionalUniqueMap<ConcurrentSocketChannel, String>> playersLoggedOn =
            new RWLockedValue<>(
                    new BidirectionalUniqueMap<>(new HashMap<>(), true));

    public static ConcurrentExclusiveMap<UUID, Triple<Map<String, UUID>, Map<String, Boolean>, QueueType>> pendingMatches =
            new ConcurrentExclusiveMap<>();
    public static int PLAYER_GAME_COUNT = 4;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(4206));
        final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        final ServerConnectionHandler serverConnectionHandler = new ServerConnectionHandler(
                serverSocketChannel, executorService
        );
        //propagate executorService to all handlers
        MessageHandler.executorService = executorService;
        GameFoundHandler.executorService = executorService;
        GameFoundTimeoutHandler.executorService = executorService;

        serverConnectionHandler.handleConnections();

        //if it reaches here, we can shutdown all tasks without executing them.
        executorService.shutdownNow();

    }
}
