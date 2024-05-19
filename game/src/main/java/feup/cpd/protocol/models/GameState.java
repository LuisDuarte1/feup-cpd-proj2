package feup.cpd.protocol.models;

import feup.cpd.game.Card;
import feup.cpd.game.Game;
import feup.cpd.protocol.models.enums.ProtocolType;
import feup.cpd.protocol.primitives.ListConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameState extends ProtocolModel{

    public final boolean isTurn;

    public final UUID matchUUID;
    public final Card topCard;
    public final List<Card> hand;

    public final List<Card> drawnCards;

    public GameState(boolean isTurn, UUID matchUUID, Card topCard, List<Card> hand, List<Card> drawnCards) {
        super(ProtocolType.GAME_STATE);
        this.isTurn = isTurn;
        this.matchUUID = matchUUID;
        this.topCard = topCard;
        this.hand = hand;
        this.drawnCards = drawnCards;
    }

    public GameState(boolean isTurn, UUID matchUUID, Card topCard, List<Card> hand) {
        super(ProtocolType.GAME_STATE);
        this.isTurn = isTurn;
        this.matchUUID = matchUUID;
        this.topCard = topCard;
        this.hand = hand;
        this.drawnCards = new ArrayList<>();
    }


    @Override
    public ByteBuffer toProtocol() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.write(isTurn ? 0x01 : 0x00);

        ByteBuffer bb = ByteBuffer.allocate(16);

        bb.putLong(matchUUID.getMostSignificantBits());
        bb.putLong(matchUUID.getLeastSignificantBits());
        byteArrayOutputStream.writeBytes(bb.array());

        byteArrayOutputStream.writeBytes(topCard.toProtocol().array());

        byteArrayOutputStream.writeBytes(new ListConverter<Card>().convertToBuffer(hand).array());

        byteArrayOutputStream.writeBytes(new ListConverter<Card>().convertToBuffer(drawnCards).array());

        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }
}
