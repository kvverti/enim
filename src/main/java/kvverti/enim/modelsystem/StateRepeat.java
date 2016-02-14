package kvverti.enim.modelsystem;

public final class StateRepeat extends StateLoop {

	StateRepeat(Token... tokens) {

		super(StatementType.REPEAT, tokens);
	}
}