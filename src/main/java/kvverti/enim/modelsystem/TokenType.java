package kvverti.enim.modelsystem;

import java.util.regex.*;

public enum TokenType {

	UNSIGNED_INTEGER	("[1-9]\\d*"),
	FLOATING_POINT		("[+-]?\\d*\\.?\\d+(?:[eE][+-]?\\d+)?"),
	COMMAND			("define|freq|rotate|shift|pause|repeat|over|\\{|\\}"),
	IDENTIFIER		("[A-Za-z_][A-Za-z_0-9]*");

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