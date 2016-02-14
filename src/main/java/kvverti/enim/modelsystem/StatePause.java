package kvverti.enim.modelsystem;

public final class StatePause extends Statement {

	StatePause(Token... t) {

		super(StatementType.PAUSE, t);
	}

	public int getPauseDuration() {

		return Integer.parseInt(getTokens()[0].getValue());
	}
}