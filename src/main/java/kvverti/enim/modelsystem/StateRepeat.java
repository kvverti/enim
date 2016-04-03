package kvverti.enim.modelsystem;

public final class StateRepeat extends StateFrameModifier {

	StateRepeat(Token... tokens) {

		super(StatementType.REPEAT, tokens);
	}
}