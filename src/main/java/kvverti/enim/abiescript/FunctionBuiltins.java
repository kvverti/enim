package kvverti.enim.abiescript;

import com.google.common.collect.ImmutableMap;

import kvverti.enim.abiescript.AbieFunctionOperator.*;

final class FunctionBuiltins {

	public static final ImmutableMap<String, AbieFunctionOperator> BUILTINS;

    //mathematical operations
    public static final AbieFunctionOperator PI;
    public static final AbieFunctionOperator E;
    public static final AbieFunctionOperator POWER;
    public static final AbieFunctionOperator UPLUS;
    public static final AbieFunctionOperator UMINUS;
    public static final AbieFunctionOperator TIMES;
    public static final AbieFunctionOperator DIVIDE;
    public static final AbieFunctionOperator REMAINDER;
    public static final AbieFunctionOperator PLUS;
    public static final AbieFunctionOperator MINUS;
    
    //builtin functions
    public static final AbieFunctionOperator ABS;
    public static final AbieFunctionOperator SIN;
    public static final AbieFunctionOperator COS;
	public static final AbieFunctionOperator TAN;
	public static final AbieFunctionOperator ASIN;
	public static final AbieFunctionOperator ACOS;
	public static final AbieFunctionOperator ATAN;
	public static final AbieFunctionOperator ARG;
    public static final AbieFunctionOperator LOG;
    public static final AbieFunctionOperator FLOOR;
    public static final AbieFunctionOperator CEIL;
    public static final AbieFunctionOperator MAX;
    public static final AbieFunctionOperator MIN;
	public static final AbieFunctionOperator CLAMP;
	public static final AbieFunctionOperator SQRT;
	public static final AbieFunctionOperator CBRT;
	public static final AbieFunctionOperator SGN;

	public static final AbieFunctionOperator PARAM;
	public static final AbieFunctionOperator COMMA;
	public static final AbieFunctionOperator LPAREN;
	public static final AbieFunctionOperator RPAREN;

	public static AbieFunctionOperator constant(double d) {

		return (Nullary) s -> s.push(d);
	}

	public static AbieFunctionOperator marker() {

		//to ensure that the instance is unique
		return new Nullary() {

			@Override
			public void eval(Stack stack) { }
		};
	}

	private static final ImmutableMap<AbieFunctionOperator, Integer> PREC;

	/**
	 * Compares by precedence. This ordering is not consistent with equals.
	 * In fact, it isn't even symmetric, so it breaks the general contract
	 * of the Comparator interface. But then again this isn't a Comparator.
	 */
	public static int compare(AbieFunctionOperator a, AbieFunctionOperator b) {

		if(a == LPAREN)
			return -1;
		if(b == LPAREN)
			return 1;
		if(a.arity() == 0)
			return b.arity() == 0 ? 0 : 1;
		if(b.arity() == 0)
			return -1;
		return PREC.getOrDefault(a, 5) - PREC.getOrDefault(b, 5);
	}

	static {

		PI = constant(Math.PI);
		E = constant(Math.E);
		POWER = (Binary) s -> { double t = s.pop(); s.push(Math.pow(s.pop(), t)); };
		UPLUS = (Unary) s -> s.push(s.pop());
		UMINUS = (Unary) s -> s.push(-s.pop());
		TIMES = (Binary) s -> s.push(s.pop() * s.pop());
		DIVIDE = (Binary) s -> { double t = s.pop(); s.push(s.pop() / t); };
		REMAINDER = (Binary) s -> { double t = s.pop(); s.push(s.pop() % t); };
		PLUS = (Binary) s -> s.push(s.pop() + s.pop());
		MINUS = (Binary) s -> s.push(-s.pop() + s.pop());
		ABS = (Unary) s -> s.push(Math.abs(s.pop()));
		SIN = (Unary) s -> s.push(Math.sin(s.pop()));
		COS = (Unary) s -> s.push(Math.cos(s.pop()));
		TAN = (Unary) s -> s.push(Math.tan(s.pop()));
		ASIN = (Unary) s -> s.push(Math.asin(s.pop()));
		ACOS = (Unary) s -> s.push(Math.acos(s.pop()));
		ATAN = (Unary) s -> s.push(Math.atan(s.pop()));
		ARG = (Binary) s -> { double t = s.pop(); s.push(Math.atan2(s.pop(), t)); };
		LOG = (Unary) s -> s.push(Math.log(s.pop()));
		FLOOR = (Unary) s -> s.push(Math.floor(s.pop()));
		CEIL = (Unary) s -> s.push(Math.ceil(s.pop()));
		MAX = (Binary) s -> s.push(Math.max(s.pop(), s.pop()));
		MIN = (Binary) s -> s.push(Math.min(s.pop(), s.pop()));
		CLAMP = (Ternary) s -> {
			double max = s.pop(), min = s.pop(), x = s.pop();
			s.push(x < min ? min : x > max ? max : x);
		};
		SQRT = (Unary) s -> s.push(Math.sqrt(s.pop()));
		CBRT = (Unary) s -> s.push(Math.cbrt(s.pop()));
		SGN = (Unary) s -> s.push(Math.signum(s.pop()));
		COMMA = (Binary) s -> {};
		PARAM = marker();
		LPAREN = marker();
		RPAREN = marker();

		BUILTINS = ImmutableMap.<String, AbieFunctionOperator>builder()
			.put("minecraft:pi", PI)
			.put("minecraft:e", E)
			.put("minecraft:sin", SIN)
			.put("minecraft:cos", COS)
			.put("minecraft:tan", TAN)
			.put("minecraft:asin", ASIN)
			.put("minecraft:acos", ACOS)
			.put("minecraft:atan", ATAN)
			.put("minecraft:arg", ARG)
			.put("minecraft:clamp", CLAMP)
			.put("minecraft:abs", ABS)
			.put("minecraft:log", LOG)
			.put("minecraft:floor", FLOOR)
			.put("minecraft:ceil", CEIL)
			.put("minecraft:max", MAX)
			.put("minecraft:min", MIN)
			.put("minecraft:sqrt", SQRT)
			.put("minecraft:cbrt", CBRT)
			.put("minecraft:sgn", SGN)
			.build();

		PREC =
			ImmutableMap.<AbieFunctionOperator, Integer>builder()
			.put(COMMA, 1)
			.put(PLUS, 2)
			.put(MINUS, 2)
			.put(TIMES, 3)
			.put(DIVIDE, 3)
			.put(REMAINDER, 3)
			.put(POWER, 4)
			.build();
	}

	private FunctionBuiltins() {

	}
}