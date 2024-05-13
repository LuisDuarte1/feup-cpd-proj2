package feup.cpd.protocol.exceptions;

public class InvalidMessage extends Exception{
    public InvalidMessage(String errorMessage) {
        super(errorMessage);
    }
}
