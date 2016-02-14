package kvverti.enim.modelsystem;

public final class StateRotate extends Statement {

	StateRotate(Token... tokens) {

		super(StatementType.ROTATE, tokens);
	}

	public String getElementToRotate() {

		return getTokens()[0].getValue();
	}

	public int getAxis() {

		return getTokens()[1].getValue().charAt(0) - 0x78;
	}

	public float getRotationAngle() {

		return Float.parseFloat(getTokens()[2].getValue());
	}

	public float getRotatedAngle(float angle) {

		float result = angle + getRotationAngle();
		if(result > 180.0f) result -= 360.0f;
		return result;
	}
}