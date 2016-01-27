package kvverti.enim.modelsystem;

public class ENIMException extends Exception {

	public ENIMException() { }

	public ENIMException(String message) {

		super(message);
	}

	public ENIMException(Throwable cause) {

		super(cause);
	}

	public ENIMException(String message, Throwable cause) {

		super(message, cause);
	}
}