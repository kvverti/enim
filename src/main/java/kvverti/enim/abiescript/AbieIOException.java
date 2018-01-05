package kvverti.enim.abiescript;

public class AbieIOException extends AbieParseException {

    public AbieIOException() { }

    public AbieIOException(String message) { super(message); }

    public AbieIOException(Throwable cause) { super(cause); }

    public AbieIOException(String message, Throwable cause) { super(message, cause); }
}