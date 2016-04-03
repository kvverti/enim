package kvverti.enim.modelsystem;

public final class AnimationFrame {

	private final StateFrameModifier modifier;
	private final StateAneme[] anemes;

	private AnimationFrame(StateFrameModifier modifier, StateAneme[] anemes) {

		this.modifier = modifier;
		this.anemes = anemes;
	}

	public static AnimationFrame compile(StateFrameModifier modifier, Statement[] anemes) throws SyntaxException {

		StateAneme[] validAnemes = new StateAneme[anemes.length];
		for(int i = 0; i < anemes.length; i++) {

			if(!anemes[i].isAneme()) throw new SyntaxException("Not an aneme: " + anemes[i]);
			validAnemes[i] = (StateAneme) anemes[i];
		}
		return new AnimationFrame(modifier, validAnemes);
	}

	public static AnimationFrame compilePause(int duration) {

		try {
			StateRepeat repeat = (StateRepeat)
				Statement.compile(StatementType.REPEAT, Token.compile(Integer.toString(duration)));
			return new AnimationFrame(repeat, new StateAneme[0]);

		  //duration was negative
		} catch(SyntaxException e) {

			throw new IllegalArgumentException(Integer.toString(duration));
		}
	}

	public StateFrameModifier modifier() {

		return modifier;
	}

	public StateAneme[] anemes() {

		return anemes.clone();
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(modifier != null ? modifier : "").append("\n{");
		for(Statement s : anemes) {

			sb.append("\n    ").append(s);
		}
		sb.append("\n}");
		return sb.toString();
	}
}