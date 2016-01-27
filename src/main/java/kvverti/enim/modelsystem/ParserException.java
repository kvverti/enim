package kvverti.enim.modelsystem;

public class ParserException extends ENIMException {

	public ParserException() { }

	public ParserException(String message) {

		super(message);
	}

	public ParserException(Throwable cause) {

		super(cause);
	}

	public ParserException(String message, Throwable cause) {

		super(message, cause);
	}
}