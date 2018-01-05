package kvverti.enim.abiescript;

public class AbieParseException extends RuntimeException {

    public AbieParseException() { }

    public AbieParseException(String message) { super(message); }

    public AbieParseException(Throwable cause) { super(cause); }

    public AbieParseException(String message, Throwable cause) { super(message, cause); }
}