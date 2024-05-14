package feup.cpd.protocol.models.factories;

import feup.cpd.protocol.models.Status;
import feup.cpd.protocol.models.enums.StatusType;
import feup.cpd.protocol.primitives.StringConverter;

import java.nio.ByteBuffer;

public class StatusFactory {

    public static Status buildFromPacket(ByteBuffer byteBuffer){
        final StringConverter stringConverter = new StringConverter();

        StatusType statusType = StatusType.fromInt(byteBuffer.getInt());
        var result = stringConverter.convertPrimitiveFromBuffer(byteBuffer);

        return new Status(statusType, result.value());
    }
}
