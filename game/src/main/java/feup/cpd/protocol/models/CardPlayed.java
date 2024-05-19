package feup.cpd.protocol.models;

import feup.cpd.game.Card;
import feup.cpd.game.Color;
import feup.cpd.game.Value;
import feup.cpd.protocol.models.enums.ProtocolType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

public class CardPlayed extends ProtocolModel{

    public final UUID matchUUID;
    public final Card card;

    public CardPlayed(UUID matchUUID, Card card){
        super(ProtocolType.CARD_PLAYED);
        this.matchUUID = matchUUID;
        this.card = card;
    }
    @Override
    public ByteBuffer toProtocol() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ByteBuffer bb = ByteBuffer.allocate(16);

        bb.putLong(matchUUID.getMostSignificantBits());
        bb.putLong(matchUUID.getLeastSignificantBits());
        byteArrayOutputStream.writeBytes(bb.array());

        byteArrayOutputStream.writeBytes(card.toProtocol().array());

        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }
}
