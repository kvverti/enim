package kvverti.enim.modelsystem;

public abstract class StateAneme extends Statement {

	StateAneme(StatementType type, Token... t) {

		super(type, t);
	}

	@Override
	public final boolean isAneme() {

		return true;
	}

	public abstract String getSpecifiedElement();
	public abstract float[] getAngles();
}