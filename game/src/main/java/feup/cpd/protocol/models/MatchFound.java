package feup.cpd.protocol.models;

import feup.cpd.protocol.models.enums.ProtocolType;

import java.nio.ByteBuffer;
import java.util.UUID;

public class MatchFound extends ProtocolModel{

    public final UUID matchID;

    public MatchFound(UUID matchID){
        super(ProtocolType.MATCH_FOUND);
        this.matchID = matchID;
    }

    @Override
    public ByteBuffer toProtocol() {
        ByteBuffer bb = ByteBuffer.allocate(16);

        bb.putLong(matchID.getMostSignificantBits());
        bb.putLong(matchID.getLeastSignificantBits());

        return bb;
    }
}
