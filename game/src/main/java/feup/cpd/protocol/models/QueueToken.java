package feup.cpd.protocol.models;

import feup.cpd.protocol.models.enums.ProtocolType;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

public class QueueToken extends ProtocolModel{
    public final UUID uuid;
    public QueueToken(UUID uuid) {
        super(ProtocolType.QUEUE_TOKEN);
        this.uuid = uuid;
    }

    @Override
    public ByteBuffer toProtocol() {
        ByteBuffer bb = ByteBuffer.allocate(16);

        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb;
    }
}
