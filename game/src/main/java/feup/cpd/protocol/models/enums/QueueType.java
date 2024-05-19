package feup.cpd.protocol.models.enums;

public enum QueueType {
    NORMAL(0),
    RANKED(1),
    ;

    public final int value;
    QueueType(int value){

        this.value = value;
    }

    public static QueueType fromInt(int value){
        for(var type : QueueType.values()){
            if(type.value == value) return type;
        }
        throw new RuntimeException("Value not found in QueueType");
    }
}
