package kvverti.enim.modelsystem;

public enum StatementType {

	DEFINITION	("define", TokenType.IDENTIFIER),
	FREQUENCY	("freq", TokenType.UNSIGNED_INTEGER),
	SET		("set", TokenType.IDENTIFIER, TokenType.AXIS, TokenType.FLOATING_POINT),
	ROTATE		("rotate", TokenType.IDENTIFIER, TokenType.AXIS, TokenType.FLOATING_POINT),
	PAUSE		("pause", TokenType.UNSIGNED_INTEGER),
	REPEAT		("repeat", TokenType.UNSIGNED_INTEGER),
	OVER		("over", TokenType.UNSIGNED_INTEGER),
	START_FRAME	("{"),
	END_FRAME	("}");

	private final String name;
	private final TokenType[] args;

	private StatementType(String s, TokenType... ttypes) {

		name = s;
		args = ttypes;
	}

	public String getName() {

		return name;
	}

	public TokenType[] getTokenTypes() {

		return args;
	}

	public int tokenCount() {

		return args.length;
	}

	public static StatementType byName(String name) {

		for(StatementType type : values()) {

			if(type.name.equals(name)) return type;
		}
		return null;
	}
}