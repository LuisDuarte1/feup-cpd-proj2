package feup.cpd.protocol.models.factories;

import feup.cpd.game.Card;
import feup.cpd.game.Color;
import feup.cpd.game.Value;
import feup.cpd.protocol.models.CardPlayed;
import feup.cpd.protocol.primitives.StringConverter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class CardFactory {

    public static List<Card> buildFromPacket(ByteBuffer byteBuffer){
        final StringConverter stringConverter = new StringConverter();

        var colorResult = stringConverter.convertPrimitiveFromBuffer(byteBuffer, null);
        var valueResult = stringConverter.convertPrimitiveFromBuffer(colorResult.rest(), null);
        var newColorResult = stringConverter.convertPrimitiveFromBuffer(valueResult.rest(), null);

        Card card = new Card(Color.valueOf(colorResult.value()), Value.valueOf(valueResult.value()));
        if(!newColorResult.value().equals("EMPTY")){
            card.setNewColor(Color.valueOf(newColorResult.value()));
        }

        return List.of(card);
    }
}
