package feup.cpd.protocol;

import feup.cpd.protocol.exceptions.InvalidMessage;
import feup.cpd.protocol.models.ProtocolModel;
import feup.cpd.protocol.models.QueueJoin;
import feup.cpd.protocol.models.enums.ProtocolType;
import feup.cpd.protocol.models.factories.LoginRequestFactory;
import feup.cpd.protocol.models.factories.QueueJoinFactory;
import feup.cpd.protocol.models.factories.QueueTokenFactory;
import feup.cpd.protocol.models.factories.StatusFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class ProtocolFacade {

    public static final byte[] MAGIC_PACKET = {0x55, 0x4E, 0x4F};

    static public ByteBuffer createPacket(ProtocolModel model){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        var bufferModel = model.toProtocol();

        byteArrayOutputStream.writeBytes(MAGIC_PACKET);
        byteArrayOutputStream.writeBytes(
                ByteBuffer.allocate(4)
                        .putInt(bufferModel.array().length)
                        .array()
        );
        byteArrayOutputStream.writeBytes(
                ByteBuffer.allocate(4)
                        .putInt(model.type.value)
                        .array()
        );

        byteArrayOutputStream.writeBytes(bufferModel.array());

        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }

    static public ProtocolModel buildFromPacket(ByteBuffer byteBuffer) throws InvalidMessage {
        byte[] magicPacketBuf = new byte[3];
        byteBuffer.get(magicPacketBuf, 0, 3);
        if(!Arrays.equals(MAGIC_PACKET, magicPacketBuf)){
            throw new InvalidMessage("Message doesn't start with MAGIC_PACKET");
        }
        ProtocolType type = ProtocolType.fromInt(byteBuffer.getInt(7));

        var restPacket = byteBuffer.position(11);
        return switch(type){
            case STATUS -> StatusFactory.buildFromPacket(restPacket);
            case LOGIN_REQUEST -> LoginRequestFactory.buildFromPacket(restPacket);
            case QUEUE_JOIN -> QueueJoinFactory.buildFromPacket(restPacket);
            case QUEUE_TOKEN -> QueueTokenFactory.buildFromPacket(restPacket);
        };
    }

    static public ProtocolModel sendModelAndReceiveResponse(SocketChannel socketChannel, ProtocolModel protocolModel)
            throws InvalidMessage, IOException {

        socketChannel.write(ProtocolFacade.createPacket(protocolModel));
        return MessageReader.readMessageFromSocket(socketChannel);

    }
}
