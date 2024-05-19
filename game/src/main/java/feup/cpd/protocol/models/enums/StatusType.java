package feup.cpd.protocol.models.enums;

public enum StatusType {
    OK(0),
    INVALID_LOGIN(1),
    INVALID_REQUEST(3),

    MATCH_STARTING(4),
    NOT_IN_QUEUE(5),
    GAME_OVER(6)
    ;

    public final int value;
    StatusType(int value){

        this.value = value;
    }

    public static StatusType fromInt(int value){
        for(var type : StatusType.values()){
            if(type.value == value) return type;
        }
        throw new RuntimeException("Value not found in ProtocolType");
    }
}
