package kvverti.enim.modelsystem;

public final class Token {

	private final String value;
	private final TokenType type;

	private Token(String s, TokenType t) {

		value = s;
		type = t;
	}

	public String getValue() {

		return value;
	}

	public TokenType getTokenType() {

		return type;
	}

	public static Token compile(String s) throws TokenSyntaxException {

		TokenType ttype = TokenType.match(s);
		if(ttype != null) {

			return new Token(s, ttype);

		} else throw new TokenSyntaxException("Unknown symbol: " + s, s);
	}

	@Override
	public String toString() {

		return String.format("[%s] %s", type, value);
	}
}