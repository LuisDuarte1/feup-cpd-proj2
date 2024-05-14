package feup.cpd.protocol.models.factories;

import feup.cpd.protocol.models.QueueJoin;
import feup.cpd.protocol.models.enums.QueueType;

import java.nio.ByteBuffer;

public class QueueJoinFactory {

    public static QueueJoin buildFromPacket(ByteBuffer byteBuffer){
        return new QueueJoin(QueueType.fromInt(byteBuffer.getInt()));
    }
}
