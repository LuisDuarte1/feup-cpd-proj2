package feup.cpd.protocol.primitives;

import java.nio.ByteBuffer;

public interface ProtocolPrimitiveConverter<T> {

    record ConversionResult<T>(T value, ByteBuffer rest){}

    ConversionResult<T> convertPrimitiveFromBuffer(ByteBuffer byteBuffer);

    ByteBuffer convertToBuffer(T primitive);

}
