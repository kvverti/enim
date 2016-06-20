package kvverti.enim.modelsystem;

import static kvverti.enim.modelsystem.TokenType.*;

public enum StatementType {

	DEFINITION	("define", IDENTIFIER),
	FREQUENCY	("freq", UNSIGNED_INTEGER),
	ROTATE		("rotate", IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	SHIFT		("shift", IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	PAUSE		("pause", UNSIGNED_INTEGER),
	REPEAT		("repeat", UNSIGNED_INTEGER),
	OVER		("over", UNSIGNED_INTEGER),
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

		return args.clone();
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