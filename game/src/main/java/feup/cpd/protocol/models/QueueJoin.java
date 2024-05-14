package feup.cpd.protocol.models;

import feup.cpd.protocol.models.enums.ProtocolType;
import feup.cpd.protocol.models.enums.QueueType;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class QueueJoin extends ProtocolModel{

    final QueueType queueType;

    public QueueJoin(QueueType queueType){
        super(ProtocolType.QUEUE_JOIN);
        this.queueType = queueType;
    }

    @Override
    public ByteBuffer toProtocol() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.writeBytes(
                ByteBuffer.allocate(4)
                        .putInt(queueType.value)
                        .array()
        );

        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }
}
