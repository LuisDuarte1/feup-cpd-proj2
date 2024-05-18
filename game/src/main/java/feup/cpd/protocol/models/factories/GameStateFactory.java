package feup.cpd.protocol.models.factories;

import feup.cpd.game.Card;
import feup.cpd.game.Game;
import feup.cpd.protocol.models.AcceptMatch;
import feup.cpd.protocol.models.GameState;

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

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                byteBuffer
                        .slice(byteBuffer.position(), byteBuffer.remaining())
                        .array()
        );
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            //trust me bro checks
            Game game = (Game) objectInputStream.readObject();
            List<Card> drawnCards = (List<Card>) objectInputStream.readObject();
            return new GameState(isTurn, new UUID(firstLong, secondLong), game, drawnCards);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
