package feup.cpd.protocol.primitives;

import feup.cpd.protocol.models.ProtocolModel;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ListConverter<T extends ProtocolModel> implements ProtocolPrimitiveConverter<List<T>>{
    @Override
    public ConversionResult<List<T>> convertPrimitiveFromBuffer(ByteBuffer byteBuffer, Function<ByteBuffer, List<T>> builder) {
        int size = byteBuffer.getInt();
        List<T> resultArray = new ArrayList<>(size);
        for(int i = 0; i < size; i++){
            int itemLength = byteBuffer.getInt();
            int currPos = byteBuffer.position();
            resultArray.add(builder.apply(byteBuffer).getFirst());
            byteBuffer.position(Math.min(currPos+itemLength, byteBuffer.capacity()));
        }

        return new ConversionResult<>(resultArray, byteBuffer);
    }

    @Override
    public ByteBuffer convertToBuffer(List<T> primitive) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.writeBytes(ByteBuffer.allocate(4).putInt(primitive.size()).array());

        for(T t : primitive){
            var resultByteBuffer = t.toProtocol();
            byteArrayOutputStream.writeBytes(ByteBuffer.allocate(4)
                    .putInt(resultByteBuffer.capacity()).array());

            byteArrayOutputStream.writeBytes(resultByteBuffer.array());
        }

        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }
}
