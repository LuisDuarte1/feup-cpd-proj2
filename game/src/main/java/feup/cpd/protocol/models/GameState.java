package feup.cpd.protocol.models;

import feup.cpd.game.Card;
import feup.cpd.game.Game;
import feup.cpd.protocol.models.enums.ProtocolType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class GameState extends ProtocolModel{

    public final boolean isTurn;

    public final UUID matchUUID;
    public final Game gameState;
    public final List<Card> drawnCards;

    public GameState(boolean isTurn, UUID matchUUID, Game gameState, List<Card> drawnCards){
        super(ProtocolType.GAME_STATE);
        this.isTurn = isTurn;
        this.matchUUID = matchUUID;
        this.gameState = gameState;
        this.drawnCards = drawnCards;
    }

    public GameState(boolean isTurn, UUID matchUUID, Game gameState){
        super(ProtocolType.GAME_STATE);
        this.isTurn = isTurn;
        this.matchUUID = matchUUID;
        this.gameState = gameState;
        this.drawnCards = List.of();
    }
    @Override
    public ByteBuffer toProtocol() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            byteArrayOutputStream.write(isTurn ? 0x01 : 0x00);

            ByteBuffer bb = ByteBuffer.allocate(16);

            bb.putLong(matchUUID.getMostSignificantBits());
            bb.putLong(matchUUID.getLeastSignificantBits());
            byteArrayOutputStream.writeBytes(bb.array());

            objectOutputStream.writeObject(gameState);
            objectOutputStream.writeObject(drawnCards);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }
}
