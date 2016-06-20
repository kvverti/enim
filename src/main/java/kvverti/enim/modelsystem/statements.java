package kvverti.enim.modelsystem;

final class StateDefine extends Statement {

	private final String define;

	StateDefine(Token... tokens) {

		super(StatementType.DEFINITION);
		define = tokens[0].getValue();
	}

	public String getDefine() {

		return define;
	}
}

final class StateFreq extends Statement {

	private final int freq;

	StateFreq(Token... tokens) {

		super(StatementType.FREQUENCY);
		freq = Integer.parseInt(tokens[0].getValue());
	}

	public int getFreq() {

		return freq;
	}
}

final class StateRotate extends StateAneme {

	private final float[] angles = new float[3];

	StateRotate(Token... tokens) {

		super(StatementType.ROTATE, tokens[0]);
		for(int i = 0; i < angles.length; i++)
			angles[i] = Float.parseFloat(tokens[i + 1].getValue());
	}

	@Override
	public float[] getAngles() {

		return angles.clone();
	}

	@Override
	public float[] getShifts() {

		return new float[3];
	}
}

final class StateShift extends StateAneme {

	private final float[] shifts = new float[3];

	StateShift(Token... tokens) {

		super(StatementType.SHIFT, tokens[0]);
		for(int i = 0; i < shifts.length; i++)
			shifts[i] = Float.parseFloat(tokens[i + 1].getValue());
	}

	@Override
	public float[] getAngles() {

		return new float[3];
	}

	@Override
	public float[] getShifts() {

		return shifts.clone();
	}
}

final class StatePause extends Statement {

	private final int duration;

	StatePause(Token... tokens) {

		super(StatementType.PAUSE);
		duration = Integer.parseInt(tokens[0].getValue());
	}

	public int getPauseDuration() {

		return duration;
	}
}

final class StateRepeat extends StateFrameModifier {

	StateRepeat(Token... tokens) {

		super(StatementType.REPEAT, tokens);
	}
}

final class StateOver extends StateFrameModifier {

	StateOver(Token... tokens) {

		super(StatementType.OVER, tokens);
	}
}

final class StateStart extends Statement {

	public static final StateStart INSTANCE = new StateStart();

	private StateStart() {

		super(StatementType.START_FRAME);
	}
}

final class StateEnd extends Statement {

	public static final StateEnd INSTANCE = new StateEnd();

	private StateEnd() {

		super(StatementType.END_FRAME);
	}
}

abstract class StateAneme extends Statement {

	private final String element;

	StateAneme(StatementType type, Token elem) {

		super(type);
		element = elem.getValue();
	}

	@Override
	public final boolean isAneme() {

		return true;
	}

	public String getSpecifiedElement() {

		return element;
	}

	public abstract float[] getAngles();

	public abstract float[] getShifts();
}

abstract class StateFrameModifier extends Statement {

	private final String modifier;

	StateFrameModifier(StatementType t, Token... ts) {

		super(t);
		modifier = ts[0].getValue();
	}

	public int getIntModifier() {

		return Integer.parseInt(modifier);
	}
}