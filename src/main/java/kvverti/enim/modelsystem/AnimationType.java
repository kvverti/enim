package kvverti.enim.modelsystem;

public enum AnimationType {

	//These are arranged in increasing order of precedence
	IDLE	("idle"),
	MOVE	("moving"),
	AIR	("airborne"),
	SWIM	("swimming"),
	JUMP	("jump"),
	ATTACK	("attack"),
	DAMAGE	("damage"),
	GREET	("greeting"),
	PART	("parting");

	private final String key;

	private AnimationType(String key) {

		this.key = key;
	}

	public String key() {

		return key;
	}
}