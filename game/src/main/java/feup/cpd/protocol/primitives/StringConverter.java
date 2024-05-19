package feup.cpd.protocol.primitives;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class StringConverter implements ProtocolPrimitiveConverter<String>{

    @Override
    public ConversionResult<String> convertPrimitiveFromBuffer(ByteBuffer byteBuffer, Function<ByteBuffer, String> builder) {
        var length = byteBuffer.getInt();
        var buff = new byte[length];
        byteBuffer.get(buff, 0, length);
        String string = new String(buff, StandardCharsets.UTF_8);
        return new ConversionResult<>(string, byteBuffer);
    }

    @Override
    public ByteBuffer convertToBuffer(String primitive) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.writeBytes(
                ByteBuffer.allocate(4)
                        .putInt(primitive.length())
                        .array()
        );

        byteArrayOutputStream.writeBytes(primitive.getBytes(StandardCharsets.UTF_8));


        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }
}
