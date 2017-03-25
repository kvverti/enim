package kvverti.enim.abiescript;

final class Token {

	private final String value;
	private final TokenType type;

	private Token(String s, TokenType t) {

		value = s;
		type = t;
	}

	public String getValue() {

		return value;
	}

	public float getFloatValue() {

		return Float.parseFloat(value);
	}

	public int getIntValue() {

		return Integer.parseInt(value);
	}

	public TokenType getTokenType() {

		return type;
	}

	public static Token compile(String s) {

		TokenType ttype = TokenType.match(s);
		if(ttype != null) {

			return new Token(s, ttype);

		} else throw new AbieSyntaxException("Could not resolve token: " + s);
	}

	@Override
	public String toString() {

		return String.format("[%s] %s", type, value);
	}
}