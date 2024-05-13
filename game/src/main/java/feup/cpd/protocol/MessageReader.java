package feup.cpd.protocol;

import feup.cpd.protocol.exceptions.InvalidMessage;
import feup.cpd.protocol.models.ProtocolModel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class MessageReader {
    static public int INITIAL_BUFFER_SIZE = 1024;

    public static ProtocolModel readMessageFromSocket(SocketChannel socketChannel) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(INITIAL_BUFFER_SIZE); //1kb buffer, messages should not be larger than that
        try {
            var r = socketChannel.read(byteBuffer);
            byteBuffer.position(0);
            if (r == -1) {
                System.out.printf("Client %s finished the connection ungracefully\n"
                        , socketChannel.getRemoteAddress());
                socketChannel.close();
                //TODO(luisd): handle disconnection in logic
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
                        //TODO(luisd): handle disconnection in logic
                        return null;
                    }
                    remainingLength -= r;
                }
            }
            byteBuffer.position(0);

            System.out.printf("Received message from %s with length %d\n",
                    socketChannel.getRemoteAddress(), length);

            return ProtocolFacade.buildFromPacket(byteBuffer);
        } catch (IOException | InvalidMessage e) {
            throw new RuntimeException(e);
        }

    }
}
