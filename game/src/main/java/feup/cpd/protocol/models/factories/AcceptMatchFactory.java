package feup.cpd.protocol.models.factories;

import feup.cpd.protocol.models.AcceptMatch;
import feup.cpd.protocol.models.MatchFound;

import java.nio.ByteBuffer;
import java.util.UUID;

public class AcceptMatchFactory {

    public static AcceptMatch buildFromPacket(ByteBuffer byteBuffer){
        boolean accepted = byteBuffer.get() == 0x01;


        long firstLong = byteBuffer.getLong();
        long secondLong = byteBuffer.getLong();

        long matchFirstLong = byteBuffer.getLong();
        long matchSecondLong = byteBuffer.getLong();
        return new AcceptMatch(accepted, new UUID(firstLong, secondLong), new UUID(matchFirstLong, matchSecondLong));
    }
}
