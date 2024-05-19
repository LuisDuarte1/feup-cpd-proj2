package feup.cpd.protocol.models.factories;

import feup.cpd.game.Card;
import feup.cpd.game.Game;
import feup.cpd.protocol.models.AcceptMatch;
import feup.cpd.protocol.models.GameState;
import feup.cpd.protocol.primitives.ListConverter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class GameStateFactory {

    @SuppressWarnings("unchecked")
    public static GameState buildFromPacket(ByteBuffer byteBuffer){
        boolean isTurn = byteBuffer.get() == 0x01;


        long firstLong = byteBuffer.getLong();
        long secondLong = byteBuffer.getLong();


        Card card = CardFactory.buildFromPacket(byteBuffer).getFirst();

        var handResult = new ListConverter<Card>().convertPrimitiveFromBuffer(byteBuffer, CardFactory::buildFromPacket);
        var drawnResult = new ListConverter<Card>().convertPrimitiveFromBuffer(handResult.rest(), CardFactory::buildFromPacket);

        return new GameState(isTurn, new UUID(firstLong,secondLong), card, handResult.value(), drawnResult.value());
    }
}
