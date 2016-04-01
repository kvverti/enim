package kvverti.enim.modelsystem;

public abstract class StateFrameModifier extends Statement {

	StateFrameModifier(StatementType t, Token... ts) {

		super(t, ts);
	}

	public int getIntModifier() {

		return Integer.parseInt(getTokens()[0].getValue());
	}
}