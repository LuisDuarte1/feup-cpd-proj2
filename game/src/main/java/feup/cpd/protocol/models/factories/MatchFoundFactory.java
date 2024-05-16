package feup.cpd.protocol.models.factories;

import feup.cpd.protocol.models.MatchFound;
import feup.cpd.protocol.models.QueueToken;

import java.nio.ByteBuffer;
import java.util.UUID;

public class MatchFoundFactory {

    public static MatchFound buildFromPacket(ByteBuffer byteBuffer){
        long firstLong = byteBuffer.getLong();
        long secondLong = byteBuffer.getLong();
        return new MatchFound(new UUID(firstLong, secondLong));
    }
}
