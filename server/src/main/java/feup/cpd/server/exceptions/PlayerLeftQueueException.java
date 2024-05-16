package feup.cpd.server.exceptions;

public class PlayerLeftQueueException extends Exception{

    public final String playerName;

    public PlayerLeftQueueException(String playerName) {
        this.playerName = playerName;
    }
}
