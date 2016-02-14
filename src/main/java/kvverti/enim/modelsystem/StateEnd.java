package kvverti.enim.modelsystem;

public final class StateEnd extends Statement {

	StateEnd(Token... t) {

		super(StatementType.END, t);
	}

	public String getName() {

		return getTokens()[0].getValue();
	}
}