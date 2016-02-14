package kvverti.enim.modelsystem;

public final class StateOver extends StateLoop {

	StateOver(Token... tokens) {

		super(StatementType.OVER, tokens);
	}

	public float interpolate(float start, float end, int frame) {

		float dec = (float) frame / (float) getLoopDuration();
		return start + (end - start) * dec;
	}
}