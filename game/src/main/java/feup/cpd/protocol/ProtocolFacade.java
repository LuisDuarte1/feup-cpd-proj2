package feup.cpd.protocol;

import feup.cpd.protocol.exceptions.InvalidMessage;
import feup.cpd.protocol.exceptions.LostConnectionException;
import feup.cpd.protocol.models.ProtocolModel;
import feup.cpd.protocol.models.QueueJoin;
import feup.cpd.protocol.models.Status;
import feup.cpd.protocol.models.enums.ProtocolType;
import feup.cpd.protocol.models.enums.StatusType;
import feup.cpd.protocol.models.factories.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;

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
        var length = byteBuffer.getInt();
        ProtocolType type = ProtocolType.fromInt(byteBuffer.getInt());
        var currPos = byteBuffer.position();

        var restPacket = byteBuffer;
        ProtocolModel protocolModel = switch(type){
            case STATUS -> StatusFactory.buildFromPacket(restPacket);
            case LOGIN_REQUEST -> LoginRequestFactory.buildFromPacket(restPacket);
            case QUEUE_JOIN -> QueueJoinFactory.buildFromPacket(restPacket);
            case QUEUE_TOKEN -> QueueTokenFactory.buildFromPacket(restPacket);
            case MATCH_FOUND -> MatchFoundFactory.buildFromPacket(restPacket);
            case ACCEPT_MATCH -> AcceptMatchFactory.buildFromPacket(restPacket);
            case GAME_STATE -> GameStateFactory.buildFromPacket(restPacket);
            case CARD_PLAYED -> CardPlayedFactory.buildFromPacket(restPacket);
            case CARD -> throw new InvalidMessage("You are not supposed to send raw CARD type");
        };
        byteBuffer.position(currPos+length);
        return protocolModel;
    }

    static public List<ProtocolModel> sendModelAndReceiveResponse(SocketChannel socketChannel, ProtocolModel protocolModel)
            throws InvalidMessage, IOException, LostConnectionException {
        if(!socketChannel.isConnected()){
            throw new LostConnectionException("Lost connection to server", protocolModel);
        }
        socketChannel.write(ProtocolFacade.createPacket(protocolModel));
        return receiveFromServer(socketChannel);
    }

    static public List<ProtocolModel> receiveFromServer(SocketChannel socketChannel) throws LostConnectionException {
        List<ProtocolModel> messages = null;
        messages = MessageReader.readMessageFromSocket(socketChannel, (Void unused) -> null);
        if(messages == null && !socketChannel.isConnected()){
            throw new LostConnectionException("Lost connection to server");
        }
        assert messages != null;
        messages.forEach((message) -> {
            switch (message){
                case Status status -> {
                    if (status.code == StatusType.INVALID_LOGIN || status.code == StatusType.INVALID_REQUEST){
                        throw new RuntimeException(
                                String.format("Server throwed status %s with message: %s",
                                        status.code,
                                        status.message));
                    }
                }
                case null -> {throw new RuntimeException("Could not build packet received from client...");}
                default -> {
                }
            }
        });

        return messages;
    }
}
