package feup.cpd.protocol.models.factories;

import feup.cpd.protocol.models.QueueToken;

import java.nio.ByteBuffer;
import java.util.UUID;

public class QueueTokenFactory {

    public static QueueToken buildFromPacket(ByteBuffer byteBuffer){
        long firstLong = byteBuffer.getLong();
        long secondLong = byteBuffer.getLong();
        return new QueueToken(new UUID(firstLong, secondLong));
    }
}
