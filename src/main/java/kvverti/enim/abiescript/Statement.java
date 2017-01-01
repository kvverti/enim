package kvverti.enim.abiescript;

import java.util.Arrays;

import kvverti.enim.Keys;
import kvverti.enim.Util;

public abstract class Statement {

	private final StatementType type;

	Statement(StatementType t) {

		type = t;
	}

	public final StatementType getStatementType() {

		return type;
	}

	public static Statement compile(String name, Token... args) {

		StatementType type = getType(name, args);
		switch(type) {

			case DEFINITION: return new StateDefine(args);
			case FREQUENCY: return new StateFreq(args);
			case ROTATE_OFFSET: return new StateRotate(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
			case ROTATE: return new StateRotate(args[0], args[1], args[2], args[3], args[4], args[5]);
			case ROTATE_LINEAR: return new StateRotate(args[0], args[1], args[2], args[3]);
			case ROTATE_NOTIME: return new StateRotate(args[0], args[1], args[2], args[3], args[4]);
			case SHIFT_OFFSET: return new StateShift(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
			case SHIFT: return new StateShift(args[0], args[1], args[2], args[3], args[4], args[5]);
			case SHIFT_LINEAR: return new StateShift(args[0], args[1], args[2], args[3]);
			case SHIFT_NOTIME: return new StateShift(args[0], args[1], args[2], args[3], args[4]);
			case PAUSE: return new StatePause(args);
			case REPEAT: return new StateRepeat(args);
			case OVER: return new StateOver(args);
			case START_FRAME: return StateStart.INSTANCE;
			case END_FRAME: return StateEnd.INSTANCE;

			default: Util.assertFalse("Unknown StatementType: " + type);
				return null; //never reached
		}
	}

	private static StatementType getType(String name, Token[] args) {

		StatementType[] types = StatementType.byName(name);
		for(StatementType type : types)
			if(validate(type, args))
				return type;
		throw new AbieSyntaxException("Invalid command or syntax for " + name + " with args " + Arrays.toString(args));
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

	@Override
	public String toString() {

		return type.getName();
	}

	public boolean isAneme() {

		return false;
	}
}