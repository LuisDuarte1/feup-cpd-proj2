package feup.cpd.protocol.models.factories;

import feup.cpd.protocol.models.LoginRequest;
import feup.cpd.protocol.primitives.StringConverter;

import java.nio.ByteBuffer;

public class LoginRequestFactory {

    public static LoginRequest buildFromPacket(ByteBuffer byteBuffer){
        final StringConverter stringConverter = new StringConverter();

        var userResult = stringConverter.convertPrimitiveFromBuffer(byteBuffer);
        var passwordResult = stringConverter.convertPrimitiveFromBuffer(userResult.rest());

        return new LoginRequest(userResult.value(), passwordResult.value());
    }

}
