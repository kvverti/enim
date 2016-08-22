package kvverti.enim.modelsystem;

import kvverti.enim.Logger;
import kvverti.enim.Util;

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

	StateRotate(Token elem, Token atype, Token time, int angleStartIndex, Token[] tokens) {

		super(StatementType.ROTATE, elem, atype, time);
		for(int i = 0; i < angles.length; i++)
			angles[i] = Float.parseFloat(tokens[i + angleStartIndex].getValue());
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

	StateShift(Token elem, Token atype, Token time, int shiftStartIndex, Token[] tokens) {

		super(StatementType.SHIFT, elem, atype, time);
		for(int i = 0; i < shifts.length; i++)
			shifts[i] = Float.parseFloat(tokens[i + shiftStartIndex].getValue());
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

	private final int angleType;
	private final int period;
	private final String element;

	StateAneme(StatementType type, Token elem, Token aType, Token time) {

		super(type);
		period = Integer.parseInt(time.getValue());
		element = elem.getValue();
		switch(aType.getValue()) {

			case Keys.ABIE_KEY_SINE: angleType = 1;
				break;
			case Keys.ABIE_KEY_COSINE: angleType = 2;
				break;
			default: Util.assertFalse("Unexpected angle type: " + aType.getValue());
			case Keys.ABIE_KEY_LINEAR: angleType = 0;
				break;
		}
	}

	@Override
	public final boolean isAneme() {

		return true;
	}

	public String getSpecifiedElement() {

		return element;
	}

	public int getAngleType() {

		return angleType;
	}

	public int getRelativePeriod() {

		return period;
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