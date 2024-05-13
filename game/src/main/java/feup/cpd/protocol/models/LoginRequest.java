package feup.cpd.protocol.models;

import feup.cpd.protocol.primitives.StringConverter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class LoginRequest extends ProtocolModel{

    public final String user;

    public final String password;

    public LoginRequest(String user, String password) {
        super(ProtocolType.LOGIN_REQUEST);
        this.user = user;
        this.password = password;
    }

    @Override
    public ByteBuffer toProtocol() {
        final StringConverter stringConverter = new StringConverter();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.writeBytes(stringConverter.convertToBuffer(user).array());
        byteArrayOutputStream.writeBytes(stringConverter.convertToBuffer(password).array());

        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
    }
}
