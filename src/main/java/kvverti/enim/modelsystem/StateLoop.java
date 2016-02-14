package kvverti.enim.modelsystem;

public abstract class StateLoop extends Statement {

	private Statement[] loopBody;

	StateLoop(StatementType t, Token... ts) {

		super(t, ts);
	}

	public final Statement[] getLoopBody() {

		return loopBody.clone();
	}

	public final void initBody(Statement... statements) {

		if(loopBody == null) {

			loopBody = statements;

		} else throw new IllegalStateException("Loop body already set");
	}

	public String getLoopName() {

		return getTokens()[1].getValue();
	}

	public int getLoopDuration() {

		return Integer.parseInt(getTokens()[0].getValue());
	}
}