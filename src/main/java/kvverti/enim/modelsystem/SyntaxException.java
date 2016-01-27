package kvverti.enim.modelsystem;

public class SyntaxException extends ENIMException {

	public SyntaxException() { }

	public SyntaxException(String message) {

		super(message);
	}

	public SyntaxException(Throwable cause) {

		super(cause);
	}

	public SyntaxException(String message, Throwable cause) {

		super(message, cause);
	}
}