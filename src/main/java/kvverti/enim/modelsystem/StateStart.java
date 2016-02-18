package kvverti.enim.modelsystem;

public final class StateStart extends Statement {

	StateStart(Token... t) {

		super(StatementType.START_FRAME, t);
	}
}