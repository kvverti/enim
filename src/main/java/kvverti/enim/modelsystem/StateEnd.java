package kvverti.enim.modelsystem;

public final class StateEnd extends Statement {

	StateEnd(Token... t) {

		super(StatementType.END_FRAME, t);
	}
}