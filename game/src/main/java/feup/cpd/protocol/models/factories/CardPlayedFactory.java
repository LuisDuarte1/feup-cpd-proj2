package feup.cpd.protocol.models.factories;

import feup.cpd.game.Card;
import feup.cpd.game.Game;
import feup.cpd.protocol.models.CardPlayed;
import feup.cpd.protocol.models.GameState;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class CardPlayedFactory {

    public static CardPlayed buildFromPacket(ByteBuffer byteBuffer){


        long firstLong = byteBuffer.getLong();
        long secondLong = byteBuffer.getLong();

        var result = CardFactory.buildFromPacket(byteBuffer).getFirst();

        return new CardPlayed(new UUID(firstLong, secondLong), result);
    }
}
