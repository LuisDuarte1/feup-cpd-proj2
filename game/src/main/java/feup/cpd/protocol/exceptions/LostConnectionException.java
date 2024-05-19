package feup.cpd.protocol.exceptions;

import feup.cpd.protocol.models.ProtocolModel;

public class LostConnectionException extends Exception{

    public final ProtocolModel protocolModel;
    public LostConnectionException(String errorMessage, ProtocolModel protocolModel) {
        super(errorMessage);
        this.protocolModel = protocolModel;
    }

    public LostConnectionException(String errorMessage) {
        super(errorMessage);
        this.protocolModel = null;
    }
}
