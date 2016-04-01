package kvverti.enim.modelsystem;

public final class StateRotate extends StateAneme {

	StateRotate(Token... tokens) {

		super(StatementType.ROTATE, tokens);
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