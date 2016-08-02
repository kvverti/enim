package kvverti.enim.modelsystem;

import java.util.Arrays;

public abstract class Statement {

	private final StatementType type;

	Statement(StatementType t) {

		type = t;
	}

	public final StatementType getStatementType() {

		return type;
	}

	public static Statement compile(String name, Token... args) throws SyntaxException {

		StatementType type = getType(name, args);
		switch(type) {

			case DEFINITION: return new StateDefine(args);
			case FREQUENCY: return new StateFreq(args);
			case ROTATE: return new StateRotate(args[2], args[0], args[1], 3, args);
			case ROTATE_LINEAR: return new StateRotate(args[0], Token.compile(Keys.ABIE_KEY_LINEAR), Token.compile("1"), 1, args);
			case ROTATE_NOTIME: return new StateRotate(args[1], args[0], Token.compile("1"), 2, args);
			case SHIFT: return new StateShift(args[2], args[0], args[1], 3, args);
			case SHIFT_LINEAR: return new StateShift(args[0], Token.compile(Keys.ABIE_KEY_LINEAR), Token.compile("1"), 1, args);
			case SHIFT_NOTIME: return new StateShift(args[1], args[0], Token.compile("1"), 2, args);
			case PAUSE: return new StatePause(args);
			case REPEAT: return new StateRepeat(args);
			case OVER: return new StateOver(args);
			case START_FRAME: return StateStart.INSTANCE;
			case END_FRAME: return StateEnd.INSTANCE;

			default: throw new IllegalArgumentException("Unknown StatementType: " + type);
		}
	}

	private static StatementType getType(String name, Token[] args) throws SyntaxException {

		StatementType[] types = StatementType.byName(name);
		for(StatementType type : types) {

			if(validate(type, args))
				return type;
		}
		throw new SyntaxException("Invalid command or syntax for " + name + " with args " + Arrays.toString(args));
	}

	private static boolean validate(StatementType type, Token[] args) {

		if(type.tokenCount() != args.length)
			return false;
		TokenType[] tokenTypes = type.getTokenTypes();
		for(int i = 0; i < args.length; i++)
			if(tokenTypes[i] != args[i].getTokenType())
				return false;
		return true;
	}

	/*private static void validate(StatementType type, Token[] args) throws SyntaxException {

		int flag = -1;
		TokenType[] types = type.getTokenTypes();
		for(int i = 0; i < types.length; i++) {

			if(types[i] != args[i].getTokenType()) flag = i;
		}
		if(flag >= 0)
			throw new SyntaxException("Unexpected argument type for statement " + type + ": "
				+ "expected " + types[flag] + ", got " + args[flag]);
	}*/

	@Override
	public String toString() {

		return type.getName();
	}

	public boolean isAneme() {

		return false;
	}
}