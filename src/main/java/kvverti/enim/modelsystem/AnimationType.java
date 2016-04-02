package kvverti.enim.modelsystem;

public enum AnimationType {

	IDLE	("idle"),
	MOVE	("moving"),
	ATTACK	("attack");

	private final String key;

	private AnimationType(String key) {

		this.key = key;
	}

	public String key() {

		return key;
	}
}