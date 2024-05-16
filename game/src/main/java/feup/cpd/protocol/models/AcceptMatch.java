package feup.cpd.protocol.models;

import feup.cpd.protocol.models.enums.ProtocolType;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

public class AcceptMatch extends ProtocolModel{

    public final boolean acceptMatchBoolean;

    public final UUID queueToken;

    public final UUID matchId;

    public AcceptMatch(boolean acceptMatchBoolean, UUID queueToken, UUID matchId){
        super(ProtocolType.ACCEPT_MATCH);
        this.acceptMatchBoolean = acceptMatchBoolean;
        this.queueToken = queueToken;
        this.matchId = matchId;
    }

    @Override
    public ByteBuffer toProtocol() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.write(acceptMatchBoolean ? 0x01 : 0x00);

        ByteBuffer bb = ByteBuffer.allocate(16);

        bb.putLong(queueToken.getMostSignificantBits());
        bb.putLong(queueToken.getLeastSignificantBits());

        byteArrayOutputStream.writeBytes(bb.array());

        ByteBuffer matchbb = ByteBuffer.allocate(16);

        matchbb.putLong(matchId.getMostSignificantBits());
        matchbb.putLong(matchId.getLeastSignificantBits());

        byteArrayOutputStream.writeBytes(matchbb.array());


        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }
}
