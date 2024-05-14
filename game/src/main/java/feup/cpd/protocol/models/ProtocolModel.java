package feup.cpd.protocol.models;

import feup.cpd.protocol.models.enums.ProtocolType;

import java.nio.ByteBuffer;

abstract public class ProtocolModel {

    ProtocolModel(ProtocolType type){
        this.type = type;
    }
    public final ProtocolType type;

    public abstract ByteBuffer toProtocol();

}
