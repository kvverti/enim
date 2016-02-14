package kvverti.enim.modelsystem;

public final class StateSet extends Statement {

	StateSet(Token... tokens) {

		super(StatementType.SET, tokens);
	}

	public String getElementToSet() {

		return getTokens()[0].getValue();
	}

	public int getAxis() {

		return getTokens()[1].getValue().charAt(0) - 0x78;
	}

	public float getAngle() {

		return Float.parseFloat(getTokens()[2].getValue());
	}
}