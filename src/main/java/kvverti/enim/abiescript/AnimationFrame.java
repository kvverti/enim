package kvverti.enim.abiescript;

import kvverti.enim.Util;

public final class AnimationFrame {

	private final StateFrameModifier modifier;
	private final StateAneme[] anemes;

	private AnimationFrame(StateFrameModifier modifier, StateAneme[] anemes) {

		this.modifier = modifier;
		this.anemes = anemes;
	}

	public static AnimationFrame compile(StateFrameModifier modifier, Statement[] anemes) {

		StateAneme[] validAnemes = new StateAneme[anemes.length];
		Util.validate(anemes, Statement::isAneme, state -> new AbieSyntaxException("Not an aneme: " + state));
		System.arraycopy(anemes, 0, validAnemes, 0, anemes.length);
		return new AnimationFrame(modifier, validAnemes);
	}

	public static AnimationFrame compilePause(int duration) {

		StateRepeat repeat = (StateRepeat) Statement.compile("repeat", Token.compile(Integer.toString(duration)));
		return new AnimationFrame(repeat, new StateAneme[0]);
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
		for(StateAneme s : anemes) {

			sb.append("\n    ").append(s);
		}
		sb.append("\n}");
		return sb.toString();
	}
}