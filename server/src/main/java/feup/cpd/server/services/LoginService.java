package feup.cpd.server.services;

import feup.cpd.protocol.ProtocolFacade;
import feup.cpd.protocol.models.GameState;
import feup.cpd.protocol.models.LoginRequest;
import feup.cpd.protocol.models.Status;
import feup.cpd.protocol.models.enums.StatusType;
import feup.cpd.server.App;
import feup.cpd.server.concurrent.ConcurrentSocketChannel;
import feup.cpd.server.concurrent.helper.LockedValue;
import feup.cpd.server.models.Player;
import feup.cpd.server.models.PlayerState;
import feup.cpd.server.repositories.NormalQueueRepository;
import feup.cpd.server.repositories.PlayerRepository;
import feup.cpd.server.repositories.RankedQueueRepository;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public class LoginService {

    private static final MessageDigest messageDigest;

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static ByteBuffer handleLoginRequest(
            LockedValue<PlayerState> playerState, LoginRequest loginRequest, ExecutorService executorService,
            ConcurrentSocketChannel concurrentSocketChannel){
        final PlayerRepository playerRepository = PlayerRepository.getInstance(executorService);

        playerState.reentrantLock.lock();
        try {
            if(playerState.value != PlayerState.UNAUTHENTICATED)
                return ProtocolFacade.createPacket(
                        new Status(StatusType.INVALID_REQUEST, "User is already logged on")
                );
        } finally {
            playerState.reentrantLock.unlock();
        }

        final byte[] receivedPasswordHash =
                messageDigest.digest(loginRequest.password.getBytes(StandardCharsets.UTF_8));

        if(!playerRepository.checkIfPlayerExists(loginRequest.user)){
            playerRepository.savePlayer(new Player(
                    loginRequest.user,
                    receivedPasswordHash
            ));
            changePlayerLoginState(playerState, loginRequest, concurrentSocketChannel, executorService);
            return ProtocolFacade.createPacket(
                    new Status(StatusType.OK, "Created account"));
        }
        final byte[] passwordHash =
                playerRepository.getFromPlayer(
                        loginRequest.user,
                        (player) -> player.passwordHash);

        if(!Arrays.equals(receivedPasswordHash, passwordHash)){
            return ProtocolFacade.createPacket(
                    new Status(StatusType.INVALID_LOGIN, "Invalid password given")
            );
        }

        changePlayerLoginState(playerState, loginRequest, concurrentSocketChannel, executorService);

        return ProtocolFacade.createPacket(
                new Status(StatusType.OK, "Login successful")
        );
    }

    private static void changePlayerLoginState(LockedValue<PlayerState> playerState, LoginRequest loginRequest,
                                               ConcurrentSocketChannel concurrentSocketChannel, ExecutorService executorService) {
        playerState.reentrantLock.lock();
        try {
            playerState.value = PlayerState.LOGGED_IN;
        } finally {
            playerState.reentrantLock.unlock();
        }
        try {
            if (NormalQueueRepository.getInstance(executorService).checkIfUserInQueue(loginRequest.user)) {
                playerState.reentrantLock.lock();
                try {
                    playerState.value = PlayerState.NORMAL_QUEUE;
                } finally {
                    playerState.reentrantLock.unlock();
                }
                return;
            }
            if (RankedQueueRepository.getInstance(executorService).checkIfUserInQueue(loginRequest.user)) {
                playerState.reentrantLock.lock();
                try {
                    playerState.value = PlayerState.RANKED_QUEUE;
                } finally {
                    playerState.reentrantLock.unlock();
                }
                return;
            }
            var lockedGame = App.activeGames.getUntilFirstInverse((gameLockedValue -> {
                gameLockedValue.reentrantLock.lock();
                try {
                    return gameLockedValue.value.checkIfPlayerBelongs(loginRequest.user);
                } finally {
                    gameLockedValue.reentrantLock.unlock();
                }
            }));
            if (lockedGame != null){
                playerState.reentrantLock.lock();
                try {
                    playerState.value = PlayerState.IN_GAME;
                } finally {
                    playerState.reentrantLock.unlock();
                }
            }
        } finally {
            App.playersLoggedOn.lockAndWrite((map) -> {
                map.put(concurrentSocketChannel, loginRequest.user);
                return  null;
            });
        }
    }
}
