package kvverti.enim.modelsystem;

public class TokenSyntaxException extends SyntaxException {

	private final String token;

	public TokenSyntaxException(String message, String token) {

		super(message);
		this.token = token;
	}

	public String getToken() {

		return token;
	}
}