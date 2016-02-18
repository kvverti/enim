package kvverti.enim.modelsystem;

import java.util.regex.*;

public final class Keys {

	public static final String IMPORTS_TAG = "imports";
	public static final String WILDCARD = "*";

	public static final String ELEMENTS_TAG = "elements";
	public static final String ELEM_NAME = "name";
	public static final String ELEM_PARENT = "parent";
	public static final String ELEM_TEXCOORDS = "uv";
	public static final String ELEM_FROM = "from";
	public static final String ELEM_TO = "to";
	public static final String ELEM_ROTPOINT = "origin";
	public static final String ELEM_DEFROT = "rotation";
	public static final String ELEM_SCALE = "scale";

	public static final String ANIMS_TAG = "animations";
	public static final String ANIM_TYPE_IDLE = "idle";
	public static final String ANIM_TYPE_MOVE = "moving";
	public static final String ANIM_TYPE_ATTACK = "attack";
	public static final String ANIM_SCRIPT = "script";
	public static final String ANIM_DEFINES = "with";

	public static final String STATES_TAG = "states";
	public static final String STATE_NORMAL = "normal";
	public static final String STATE_MODEL_NAME = "model";
	public static final String STATE_MODEL_WEIGHT = "weight";
	public static final String STATE_TEXTURE = "texture";
	public static final String STATE_TEX_SIZE = "size";
	public static final String STATE_ROTATION = "rotation";
	public static final String STATE_SCALE = "scale";

	public static final String STATES_DIR = "entitystates/";
	public static final String MODELS_DIR = "models/entity/";
	public static final String TEXTURES_DIR = "textures/";

	public static final String JSON = ".json";
	public static final String PNG = ".png";
	public static final String ENIM = ".enim";

	public static final Pattern IDENTIFIER_REGEX = Pattern.compile("[A-Za-z_][A-Za-z_0-9]*");
	public static final Pattern RESOURCE_LOCATION_REGEX =
		Pattern.compile("(?:(?<domain>[A-Za-z_0-9]+):)?(?<filepath>[A-Za-z_0-9/]+)");

	private Keys() { }
}