package feup.cpd.protocol.models;


import feup.cpd.protocol.models.enums.ProtocolType;
import feup.cpd.protocol.models.enums.StatusType;
import feup.cpd.protocol.primitives.StringConverter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Status extends ProtocolModel{


    public final StatusType code;

    public final String message;

    public Status(StatusType code, String message) {
        super(ProtocolType.STATUS);
        this.code = code;
        this.message = message;
    }

    @Override
    public ByteBuffer toProtocol() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.writeBytes(
                ByteBuffer.allocate(4)
                        .putInt(code.value)
                        .array()
        );

        byteArrayOutputStream.writeBytes(
                (new StringConverter())
                        .convertToBuffer(message)
                        .array()
        );

        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }
}
