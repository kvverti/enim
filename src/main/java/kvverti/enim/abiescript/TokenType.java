package kvverti.enim.abiescript;

import java.util.regex.*;

import kvverti.enim.Keys;

enum TokenType {

	UNSIGNED_INTEGER    ("[1-9]\\d*"),
	FLOATING_POINT      ("[+-]\\d*\\.?\\d+(?:[eE][+-]?\\d+)?"),
	COMMAND             (Keys.ABIE_KEY_DEFINE
	             + "|" + Keys.ABIE_KEY_FREQ
	             + "|" + Keys.ABIE_KEY_ROTATE
	             + "|" + Keys.ABIE_KEY_SHIFT
	             + "|" + Keys.ABIE_KEY_PAUSE
	             + "|" + Keys.ABIE_KEY_REPEAT
	             + "|" + Keys.ABIE_KEY_OVER
				 + "|" + Keys.ABIE_KEY_INIT
	             + "|" + "\\{"
	             + "|" + "\\}"),
	ANGLE_TYPE          (Keys.ABIE_KEY_LINEAR + "|" + Keys.ABIE_KEY_SINE + "|" + Keys.ABIE_KEY_COSINE),
	IDENTIFIER          ("[a-z_][a-z_0-9]*");

	private final Pattern regex;

	private TokenType(String p) {

		regex = Pattern.compile(p);
	}

	public Matcher matcher(CharSequence s) {

		return regex.matcher(s);
	}

	public Pattern pattern() {

		return regex;
	}

	public static TokenType match(CharSequence s) {

		for(TokenType t : values()) {

			if(t.matcher(s).matches()) return t;
		}
		return null;
	}
}