package kvverti.enim.abiescript;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.regex.*;
import java.util.function.DoubleUnaryOperator;

/** Class that represents custom defined functions. Yes it's a parser inside a parser :\ */
final class StateFunction extends Statement implements StateAneme.AnemeTransformation {

	private final String fname;
	private final String fbody;
	private Deque<AbieFunctionOperator> code;

	StateFunction(Token name, Token body) {

		super(StatementType.FUNCTION);
		fname = name.getValue();
		String tmp = body.getValue();
		fbody = tmp.substring(2, tmp.length() - 1);
	}

	@Override
	public DoubleUnaryOperator apply(double om, double ph) {

		assert code != null;
		return new FunctionImpl(code, om, ph);
	}

	public String getName() { return fname; }

	private static final Pattern WS = Pattern.compile("\\s+");
	private static final Pattern FUNC = Pattern.compile("(?:(?<domain>[a-z_0-9]+):)?(?<filepath>[a-z_0-9]+)");
	private static final Pattern NUM = Pattern.compile("[+-]?\\d*\\.?\\d+(?:[eE][+-]?\\d+)?");
	private static final Pattern OP = Pattern.compile("\\*\\*|[*/%+\\-,]");

	/** Parse and create a function from the body. */
	void init(Map<String, StateAneme.AnemeTransformation> functions) {

		assert code == null;
		//list of operators we will sort later
		List<AbieFunctionOperator> ls = new ArrayList<>();
		//index of the string we are at
		int idx = 0;
		//are we expecting an operand (function, number) or an operator
		boolean operand = true;
		Matcher m = WS.matcher(fbody);
		//turn each sub-token into an operator
		while(idx < fbody.length()) {

			m.region(idx, fbody.length());
			if(m.usePattern(WS).lookingAt()) //whitespace
				idx = m.end();
			else if(operand) {

				if(fbody.charAt(idx) == 't') {
					//the implicit parameter
					ls.add(FunctionBuiltins.PARAM);
					idx++;
					operand = false;
				} else if(fbody.charAt(idx) == '(') {

					ls.add(FunctionBuiltins.LPAREN);
					idx++;
				} else if(m.usePattern(NUM).lookingAt()) {
					//why do I make intentionally verbose names???
					double d = Double.parseDouble(m.group());
					ls.add(FunctionBuiltins.constant(d));
					idx = m.end();
					operand = false;
				} else if(m.usePattern(OP).lookingAt()) {
					//prefix operator
					AbieFunctionOperator op;
					switch(m.group().charAt(0)) {

						case '+': op = FunctionBuiltins.UPLUS; break;
						case '-': op = FunctionBuiltins.UMINUS; break;
						default: throw new AbieSyntaxException("Function " + fname + ": expected + or -");
					}
					ls.add(op);
					idx = m.end();
				} else if(m.usePattern(FUNC).lookingAt()) {
					//user defined or builtin function
					String domain = m.group("domain");
					String name = m.group("filepath");
					if(domain == null)
						domain = "minecraft";
					String f = domain + ":" + name;
					StateAneme.AnemeTransformation sf = functions.get(f);
					AbieFunctionOperator op;
					if(sf == null) {
						op = FunctionBuiltins.BUILTINS.get(f);
						if(op == null)
							throw new AbieParseException("Function " + fname + ": no such function " + f);
					} else
						op = (AbieFunctionOperator) sf.apply(1.0, 0.0);
					ls.add(op);
					idx = m.end();
					if(op.arity() == 0)
						operand = false;
				} else
					throw new AbieSyntaxException("Function " + fname + ": expected operand at " + idx);
			} else {
				if(fbody.charAt(idx) == ')') {

					ls.add(FunctionBuiltins.RPAREN);
					idx++;
				} else if(m.usePattern(OP).lookingAt()) {

					String name = m.group();
					AbieFunctionOperator op;
					if(name.equals("**"))
						op = FunctionBuiltins.POWER;
					else {
						switch(name.charAt(0)) {

							case '*': op = FunctionBuiltins.TIMES; break;
							case '/': op = FunctionBuiltins.DIVIDE; break;
							case '%': op = FunctionBuiltins.REMAINDER; break;
							case '+': op = FunctionBuiltins.PLUS; break;
							case '-': op = FunctionBuiltins.MINUS; break;
							case ',': op = FunctionBuiltins.COMMA; break;
							default: throw new AbieSyntaxException("Function " + fname + ": expected operator at " + idx);
						}
					}
					ls.add(op);
					idx = m.end();
					operand = true;
				} else
					throw new AbieSyntaxException("Function " + fname + ": expected operator at " + idx);
			}
		}
		//ls now contains the body of the function in infix order
		//now we turn it into postfix order using the
		//shunting yard algorithm
		int stackSize = 0; //make sure we have a balanced stack when we evaluate
		Deque<AbieFunctionOperator> out = new ArrayDeque<>();
		Deque<AbieFunctionOperator> ops = new ArrayDeque<>();
		for(AbieFunctionOperator op : ls) {
			if(op == FunctionBuiltins.LPAREN)
				ops.push(op);
			else if(op == FunctionBuiltins.RPAREN) {
				//shunt operators until we hit a left paren
				AbieFunctionOperator tmp = ops.poll();
				while(tmp != FunctionBuiltins.LPAREN) {
					if(tmp == null)
						throw new AbieSyntaxException("Unbalanced brackets in function " + fname);
					stackSize -= tmp.arity() - 1;
					if(stackSize < 0)
						throw new AbieParseException("Invalid function " + fname);
					out.add(tmp);
					tmp = ops.poll();
				}
				//left paren was just popped
			} else if(op.arity() == 0) {
				//push arity zero ops over to output
				out.add(op);
				stackSize++;
			} else {
				//figure out #precedence
				//pop off operators of greater or equal precedence
				int c = op == FunctionBuiltins.POWER ? 0 : 1;
				while(!ops.isEmpty() && FunctionBuiltins.compare(op, ops.peek()) - c < 0) {
					AbieFunctionOperator tmp = ops.pop();
					assert tmp != FunctionBuiltins.RPAREN : "RPAREN";
					assert tmp != FunctionBuiltins.COMMA : "COMMA";
					stackSize -= tmp.arity() - 1;
					if(stackSize < 0)
						throw new AbieParseException("Invalid function " + fname);
					out.add(tmp);
				}
				//add operator to ops
				if(op != FunctionBuiltins.COMMA)
					ops.push(op);
			}
		}
		//add any leftover operators to out
		while(!ops.isEmpty()) {
			AbieFunctionOperator tmp = ops.pop();
			stackSize -= tmp.arity() - 1;
			if(stackSize < 0)
				throw new AbieParseException("Invalid function " + fname);
			out.add(tmp);
		}
		if(stackSize != 1)
			throw new AbieParseException("Invalid function " + fname);
		this.code = out;
	}

	@Override
	public String toString() {

		return super.toString() + " " + fname + " " + fbody;
	}

	/** Implementation of functions */
	private static class FunctionImpl implements DoubleUnaryOperator, AbieFunctionOperator.Unary {

		private final double om;
		private final double ph;
		private final Deque<AbieFunctionOperator> code;
		private double t;

		FunctionImpl(Deque<AbieFunctionOperator> c, double o, double p) {

			code = c;
			om = o;
			ph = p;
		}

		@Override
		public double applyAsDouble(double arg) {

			Stack s = new Stack(10);
			s.push(om * arg + ph);
			eval(s);
			return s.pop();
		}

		@Override
		public void eval(Stack stack) {

			t = stack.pop();
			for(AbieFunctionOperator op : code) {

				if(op == FunctionBuiltins.PARAM)
					stack.push(t);
				else
					op.eval(stack);
			}
		}
	}
}