package kvverti.enim.modelsystem;

public final class StateSet extends StateAneme {

	StateSet(Token... tokens) {

		super(StatementType.SET, tokens);
	}

	@Override
	public String getSpecifiedElement() {

		return getTokens()[0].getValue();
	}

	@Override
	public float[] getAngles() {

		float[] angles = new float[3];
		for(int i = 0; i < 3; i++) {

			angles[i] = Float.parseFloat(getTokens()[i + 1].getValue());
		}
		return angles;
	}
}