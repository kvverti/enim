package kvverti.enim.modelsystem;

public final class StateDefine extends Statement {

	StateDefine(Token... t) {

		super(StatementType.DEFINITION, t);
	}

	public String getDefine() {

		return getTokens()[0].getValue();
	}
}