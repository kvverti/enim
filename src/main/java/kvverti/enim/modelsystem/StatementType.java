package kvverti.enim.modelsystem;

import java.util.List;
import java.util.ArrayList;

import static kvverti.enim.modelsystem.TokenType.*;

public enum StatementType {

	DEFINITION	("define", IDENTIFIER),
	FREQUENCY	("freq", UNSIGNED_INTEGER),
	ROTATE		("rotate", IDENTIFIER, UNSIGNED_INTEGER, IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	ROTATE_LINEAR	("rotate", IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	ROTATE_NOTIME	("rotate", IDENTIFIER, IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	SHIFT		("shift", IDENTIFIER, UNSIGNED_INTEGER, IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	SHIFT_LINEAR	("shift", IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	SHIFT_NOTIME	("shift", IDENTIFIER, IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
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

	public static StatementType[] byName(String name) {

		List<StatementType> types = new ArrayList<>();
		for(StatementType type : values()) {

			if(type.name.equals(name))
				types.add(type);
		}
		return types.toArray(new StatementType[types.size()]);
	}
}