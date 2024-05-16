package feup.cpd.server.models;

public enum PlayerState {

    UNAUTHENTICATED(0),
    LOGGED_IN(1),
    NORMAL_QUEUE(2),
    RANKED_QUEUE(3),
    IN_GAME(4),

    FOUND_GAME(5),
    ;

    public final int value;
    PlayerState(int value){

        this.value = value;
    }

    public static PlayerState fromInt(int value){
        for(var type : PlayerState.values()){
            if(type.value == value) return type;
        }
        throw new RuntimeException("Value not found in ProtocolType");
    }
}
