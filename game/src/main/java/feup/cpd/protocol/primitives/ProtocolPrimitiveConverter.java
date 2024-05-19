package feup.cpd.protocol.primitives;

import java.nio.ByteBuffer;
import java.util.function.Function;

public interface ProtocolPrimitiveConverter<T> {

    record ConversionResult<T>(T value, ByteBuffer rest){}

    ConversionResult<T> convertPrimitiveFromBuffer(ByteBuffer byteBuffer, Function<ByteBuffer, T> builder);

    ByteBuffer convertToBuffer(T primitive);

}
