package kvverti.enim.modelsystem;

public abstract class Statement {

	private final Token[] args;
	private final StatementType type;

	Statement(StatementType t, Token... ts) {

		args = ts;
		type = t;
	}

	/* Guaranteed to be syntactically correct */
	public final Token[] getTokens() {

		return args;
	}

	public final StatementType getStatementType() {

		return type;
	}

	public final int tokenCount() {

		return args.length;
	}

	public static Statement compile(StatementType type, Token... args) throws SyntaxException {

		validate(type, args);
		switch(type) {

			case DEFINITION: return new StateDefine(args);
			case FREQUENCY: return new StateFreq(args);
			case SET: return new StateSet(args);
			case ROTATE: return new StateRotate(args);
			case PAUSE: return new StatePause(args);
			case REPEAT: return new StateRepeat(args);
			case OVER: return new StateOver(args);
			case END: return new StateEnd(args);

			default: throw new IllegalArgumentException("Unknown StatementType: " + type);
		}
	}

	private static void validate(StatementType type, Token[] args) throws SyntaxException {

		int flag = -1;
		TokenType[] types = type.getTokenTypes();
		for(int i = 0; i < types.length; i++) {

			if(types[i] != args[i].getTokenType()) flag = i;
		}
		if(flag >= 0)
			throw new SyntaxException("Unexpected argument type for statement " + type + ": "
				+ "expected " + types[flag] + ", got " + args[flag]);
	}

	@Override
	public final String toString() {

		StringBuilder sb = new StringBuilder().append(type.getName());
		for(Token t : args) {

			sb.append(' ').append(t.getValue());
		}
		return sb.toString();
	}
}