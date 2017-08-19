package kvverti.enim.abiescript;

import java.util.function.DoubleUnaryOperator;

import com.google.common.collect.ImmutableMap;

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

	private StateRotate(String atype, float time, float offset, String elem, float x, float y, float z) {

		super(StatementType.ROTATE, atype, time, offset, elem);
		this.angles = Vec3f.of(x, y, z);
	}

	StateRotate(Token atype, Token time, Token offset, Token elem, Token x, Token y, Token z) {

		this(atype.getValue(), time.getFloatValue(), offset.getFloatValue(), elem.getValue(), x.getFloatValue(), y.getFloatValue(), z.getFloatValue());
	}

	StateRotate(Token atype, Token time, Token elem, Token x, Token y, Token z) {

		this(atype.getValue(), time.getFloatValue(), 0.0f, elem.getValue(), x.getFloatValue(), y.getFloatValue(), z.getFloatValue());
	}

	StateRotate(Token atype, Token elem, Token x, Token y, Token z) {

		this(atype.getValue(), 1.0f, 0.0f, elem.getValue(), x.getFloatValue(), y.getFloatValue(), z.getFloatValue());
	}

	StateRotate(Token elem, Token x, Token y, Token z) {

		this("linear", 1.0f, 0.0f, elem.getValue(), x.getFloatValue(), y.getFloatValue(), z.getFloatValue());
	}

	@Override
	public Vec3f[] getTransforms() {

		return new Vec3f[] { angles, Vec3f.ORIGIN };
	}
}

final class StateShift extends StateAneme {

	private final Vec3f shifts;

	private StateShift(String atype, float time, float offset, String elem, float x, float y, float z) {

		super(StatementType.SHIFT, atype, time, offset, elem);
		this.shifts = Vec3f.of(x, y, z);
	}

	StateShift(Token atype, Token time, Token offset, Token elem, Token x, Token y, Token z) {

		this(atype.getValue(), time.getFloatValue(), offset.getFloatValue(), elem.getValue(), x.getFloatValue(), y.getFloatValue(), z.getFloatValue());
	}

	StateShift(Token atype, Token time, Token elem, Token x, Token y, Token z) {

		this(atype.getValue(), time.getFloatValue(), 0.0f, elem.getValue(), x.getFloatValue(), y.getFloatValue(), z.getFloatValue());
	}

	StateShift(Token atype, Token elem, Token x, Token y, Token z) {

		this(atype.getValue(), 1.0f, 0.0f, elem.getValue(), x.getFloatValue(), y.getFloatValue(), z.getFloatValue());
	}

	StateShift(Token elem, Token x, Token y, Token z) {

		this("linear", 1.0f, 0.0f, elem.getValue(), x.getFloatValue(), y.getFloatValue(), z.getFloatValue());
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

final class StateInit extends StateFrameModifier {

	public static final StateInit INSTANCE = new StateInit();

	private StateInit() {

		super(StatementType.INIT, (Token) null);
	}
}

final class StateMarker extends Statement {

	public static final StateMarker START = new StateMarker(StatementType.START_FRAME);
	public static final StateMarker END = new StateMarker(StatementType.END_FRAME);

	private StateMarker(StatementType type) {

		super(type);
	}
}

abstract class StateAneme extends Statement {

	/** Primitive specialization of BiFunction for double parameters and returning DoubleUnaryOperator. */
	@FunctionalInterface
	private interface AnemeTransformation { DoubleUnaryOperator apply(double a, double b); }

	/**
	 * Factories for functions of the form f(t) = T(ωt + φ), where t ranges 0 <= t <= 1.
	 * The results are multiplied by the amplitude Δx corresponding to values in {@link #getTransforms()}.
	 * Thus, the complete transformation is given by dx = Δxf(t).
	 */
	private static final ImmutableMap<String, AnemeTransformation> functionFactories =
		ImmutableMap.<String, AnemeTransformation>builder()
		.put(Keys.ABIE_KEY_LINEAR, (ω, φ) -> t -> ω * t + φ)
		.put(Keys.ABIE_KEY_SINE, (ω, φ) -> t -> Math.sin(2.0 * Math.PI * (ω * t + φ)))
		.put(Keys.ABIE_KEY_COSINE, (ω, φ) -> t -> 0.5 * (1.0 - Math.cos(2.0 * Math.PI * (ω * t + φ))))
		.put(Keys.ABIE_KEY_CUBIC, (ω, φ) -> t -> Math.pow(ω * t + φ, 3))
		.build();

	private final String element;
	private final DoubleUnaryOperator function;

	StateAneme(StatementType type, String aType, float time, float offset, String elem) {

		super(type);
		element = elem;
		function = functionFactories.get(aType).apply(time, offset);
	}

	@Override
	public final boolean isAneme() {

		return true;
	}

	public String getSpecifiedElement() {

		return element;
	}

	public DoubleUnaryOperator getTransformationFunction() {

		return function;
	}

	public abstract Vec3f[] getTransforms();
}

abstract class StateFrameModifier extends Statement {

	private final Token modifier;

	StateFrameModifier(StatementType t, Token... ts) {

		super(t);
		modifier = ts[0];
	}

	public int getIntModifier() {

		return modifier.getIntValue();
	}
}