package feup.cpd.protocol;

import feup.cpd.protocol.exceptions.InvalidMessage;
import feup.cpd.protocol.models.ProtocolModel;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class MessageReader {
    //10kb buffer, messages should not be larger than that
    static public int INITIAL_BUFFER_SIZE = 1024*1024;

    public static List<ProtocolModel> readMessageFromSocket(SocketChannel socketChannel,
                                                            Function<Void, Void> disconnectionCallback) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);
        try {
            var r = socketChannel.read(byteBuffer);
            byteBuffer.position(0);
            if (r == -1) {
                System.out.printf("Client %s finished the connection ungracefully\n"
                        , socketChannel.getRemoteAddress());
                socketChannel.close();
                disconnectionCallback.apply(null);
                return null;
            }

            var magicBytesBuf = new byte[3];
            byteBuffer.get(magicBytesBuf, 0, 3);

            if (!Arrays.equals(magicBytesBuf, ProtocolFacade.MAGIC_PACKET)) {
                System.out.printf("Client %s sent a wrongly formatted packet... ignoring.\n",
                        socketChannel.getRemoteAddress());
                return null;
            }

            var length = byteBuffer.getInt();

            if (length > INITIAL_BUFFER_SIZE) {
                var temp = byteBuffer;
                byteBuffer = ByteBuffer.allocate(length);
                byteBuffer.put(temp.array(), 0, r-7);
                byteBuffer.position(r);

                var remainingLength = length - r;

                while (remainingLength > 0) {
                    r = socketChannel.read(byteBuffer);
                    if (r == -1) {
                        System.out.printf("Client %s finished the connection ungracefully\n"
                                , socketChannel.getRemoteAddress());
                        socketChannel.close();
                        disconnectionCallback.apply(null);
                        return null;
                    }
                    remainingLength -= r;
                }
            }
            byteBuffer.position(0);

            System.out.printf("Received message from %s with length %d\n",
                    socketChannel.getRemoteAddress(), length);
            final List<ProtocolModel> protocolModelList = new ArrayList<>();
            ProtocolModel protocolModel = ProtocolFacade.buildFromPacket(byteBuffer);
            protocolModelList.add(protocolModel);
            while (true){
                try {
                    ProtocolModel otherModel = ProtocolFacade.buildFromPacket(byteBuffer);
                    protocolModelList.add(otherModel);
                } catch (InvalidMessage invalidMessage){
                    break;
                }

            }
            return protocolModelList;
        } catch (ClosedChannelException closedChannelException){
            //don't call another message handler if it's closed
            return null;
        } catch (ConnectException e){
            disconnectionCallback.apply(null);
            return null;
        }
        catch (IOException | InvalidMessage e) {
            throw new RuntimeException(e);
        }

    }
}
