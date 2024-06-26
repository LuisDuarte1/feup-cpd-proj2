package feup.cpd.protocol.models.enums;

public enum ProtocolType {
    STATUS(0),
    LOGIN_REQUEST(1),

    QUEUE_JOIN(2),
    QUEUE_TOKEN(3),
    MATCH_FOUND(4),
    ACCEPT_MATCH(5),
    GAME_STATE(6),
    CARD_PLAYED(7),
    CARD(8)

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
