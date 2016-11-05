package kvverti.enim.abiescript;

import kvverti.enim.Keys;
import kvverti.enim.Util;
import kvverti.enim.Vec3f;

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

	private final Vec3f angles;

	StateRotate(Token elem, Token atype, Token time, int angleStartIndex, Token[] tokens) {

		super(StatementType.ROTATE, elem, atype, time);
		float[] angles = new float[3];
		for(int i = 0; i < angles.length; i++)
			angles[i] = Float.parseFloat(tokens[i + angleStartIndex].getValue());
		this.angles = Vec3f.of(angles[0], angles[1], angles[2]);
	}

	@Override
	public Vec3f[] getTransforms() {

		return new Vec3f[] { angles, Vec3f.ORIGIN };
	}
}

final class StateShift extends StateAneme {

	private final Vec3f shifts;

	StateShift(Token elem, Token atype, Token time, int shiftStartIndex, Token[] tokens) {

		super(StatementType.SHIFT, elem, atype, time);
		float[] shifts = new float[3];
		for(int i = 0; i < shifts.length; i++)
			shifts[i] = Float.parseFloat(tokens[i + shiftStartIndex].getValue());
		this.shifts = Vec3f.of(shifts[0], shifts[1], shifts[2]);
	}

	@Override
	public Vec3f[] getTransforms() {

		return new Vec3f[] { Vec3f.ORIGIN, shifts };
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

	public abstract Vec3f[] getTransforms();
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