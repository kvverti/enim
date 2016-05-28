package kvverti.enim.modelsystem;

public enum AnimationType {

	IDLE	("idle"),
	MOVE	("moving"),
	JUMP	("jump"),
	SWIM	("swimming"),
	FLY	("flying"),
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