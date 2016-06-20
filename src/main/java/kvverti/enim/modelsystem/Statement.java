package kvverti.enim.modelsystem;

public abstract class Statement {

	private final StatementType type;

	Statement(StatementType t) {

		type = t;
	}

	public final StatementType getStatementType() {

		return type;
	}

	public static Statement compile(StatementType type, Token... args) throws SyntaxException {

		validate(type, args);
		switch(type) {

			case DEFINITION: return new StateDefine(args);
			case FREQUENCY: return new StateFreq(args);
			case ROTATE: return new StateRotate(args);
			case SHIFT: return new StateShift(args);
			case PAUSE: return new StatePause(args);
			case REPEAT: return new StateRepeat(args);
			case OVER: return new StateOver(args);
			case START_FRAME: return StateStart.INSTANCE;
			case END_FRAME: return StateEnd.INSTANCE;

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
	public String toString() {

		return type.getName();
	}

	public boolean isAneme() {

		return false;
	}
}