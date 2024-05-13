package feup.cpd.server.services;

import feup.cpd.protocol.ProtocolFacade;
import feup.cpd.protocol.models.LoginRequest;
import feup.cpd.protocol.models.Status;
import feup.cpd.protocol.models.StatusType;
import feup.cpd.server.models.Player;
import feup.cpd.server.repositories.PlayerRepository;

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

    public static ByteBuffer handleLoginRequest(LoginRequest loginRequest, ExecutorService executorService){
        final PlayerRepository playerRepository = PlayerRepository.getInstance(executorService);

        final byte[] receivedPasswordHash =
                messageDigest.digest(loginRequest.password.getBytes(StandardCharsets.UTF_8));

        if(!playerRepository.checkIfPlayerExists(loginRequest.user)){
            playerRepository.savePlayer(new Player(
                    loginRequest.user,
                    receivedPasswordHash
            ));
            return ProtocolFacade.createPacket(
                    new Status(StatusType.LOGIN_SUCCESSFUL, "Created account"));
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

        return ProtocolFacade.createPacket(
                new Status(StatusType.OK, "Login successful")
        );
    }
}
