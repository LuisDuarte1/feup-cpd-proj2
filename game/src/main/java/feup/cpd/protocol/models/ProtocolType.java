package feup.cpd.protocol.models;

import java.util.Map;

public enum ProtocolType {
    STATUS(0),
    LOGIN_REQUEST(1),

    ;

    public final int value;
    ProtocolType(int value){

        this.value = value;
    }

    public static ProtocolType fromInt(int value){
        for(var type : ProtocolType.values()){
            if(type.value == value) return type;
        }
        throw new RuntimeException("Value not found in ProtocolType");
    }
}
