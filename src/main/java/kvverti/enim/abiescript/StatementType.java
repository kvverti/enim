package kvverti.enim.abiescript;

import java.util.List;
import java.util.ArrayList;

import kvverti.enim.Keys;

import static kvverti.enim.abiescript.TokenType.*;

public enum StatementType {

	DEFINITION       (Keys.ABIE_KEY_DEFINE, IDENTIFIER),
	FREQUENCY        (Keys.ABIE_KEY_FREQ, UNSIGNED_INTEGER),
	ROTATE_OFFSET    (Keys.ABIE_KEY_ROTATE, ANGLE_TYPE, FLOATING_POINT, FLOATING_POINT, IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	ROTATE           (Keys.ABIE_KEY_ROTATE, ANGLE_TYPE, FLOATING_POINT, IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	ROTATE_LINEAR    (Keys.ABIE_KEY_ROTATE, IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	ROTATE_NOTIME    (Keys.ABIE_KEY_ROTATE, ANGLE_TYPE, IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	SHIFT_OFFSET     (Keys.ABIE_KEY_SHIFT, ANGLE_TYPE, FLOATING_POINT, FLOATING_POINT, IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	SHIFT            (Keys.ABIE_KEY_SHIFT, ANGLE_TYPE, FLOATING_POINT, IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	SHIFT_LINEAR     (Keys.ABIE_KEY_SHIFT, IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	SHIFT_NOTIME     (Keys.ABIE_KEY_SHIFT, ANGLE_TYPE, IDENTIFIER, FLOATING_POINT, FLOATING_POINT, FLOATING_POINT),
	PAUSE            (Keys.ABIE_KEY_PAUSE, UNSIGNED_INTEGER),
	REPEAT           (Keys.ABIE_KEY_REPEAT, UNSIGNED_INTEGER),
	OVER             (Keys.ABIE_KEY_OVER, UNSIGNED_INTEGER),
	START_FRAME      ("{"),
	END_FRAME        ("}");

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