package kvverti.enim.modelsystem;

public class LexerException extends ParserException {

	public LexerException() { }

	public LexerException(String message) {

		super(message);
	}

	public LexerException(Throwable cause) {

		super(cause);
	}

	public LexerException(String message, Throwable cause) {

		super(message, cause);
	}
}