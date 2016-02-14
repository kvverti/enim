package kvverti.enim.modelsystem;

public final class StateFreq extends Statement {

	StateFreq(Token... t) {

		super(StatementType.FREQUENCY, t);
	}

	public int getFreq() {

		return Integer.parseInt(getTokens()[0].getValue());
	}
}