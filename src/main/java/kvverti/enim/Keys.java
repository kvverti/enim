package kvverti.enim;

import java.util.regex.*;

public final class Keys {

	public static final String PROPERTIES_TAG = "properties";
//	public static final String PROP_BOUNDING_BOX = "bounds";
	public static final String PROP_NAMETAG_ORIGIN = "nameplate";
	public static final String PROP_HELD_ITEM_ORIGIN_RIGHT = "righthand";
	public static final String PROP_HELD_ITEM_ORIGIN_LEFT = "lefthand";
	public static final String PROP_META_PARENT = "parent";
	public static final String PROP_META_ORIGIN = "origin";

	public static final String IMPORTS_TAG = "imports";
	public static final String WILDCARD = "*";

	public static final String ELEMENTS_TAG = "elements";
	public static final String ELEMENTS_OVERRIDES = "overrides";
	public static final String ELEM_NAME = "name";
	public static final String ELEM_PARENT = "parent";
	public static final String ELEM_TEXCOORDS = "uv";
	public static final String ELEM_FROM = "from";
	public static final String ELEM_TO = "to";
	public static final String ELEM_ROTPOINT = "origin";
	public static final String ELEM_DEFROT = "rotation";
	public static final String ELEM_SCALE = "scale";
	public static final String ELEM_TRANSLUCENT = "translucent";
	public static final String ELEM_HEAD = "head";

	public static final String ANIMS_TAG = "animations";
	public static final String ANIM_SCRIPT = "script";
	public static final String ANIM_DEFINES = "with";

	public static final String ABIE_KEY_DEFINE = "define";
	public static final String ABIE_KEY_FREQ = "freq";
	public static final String ABIE_KEY_REPEAT = "repeat";
	public static final String ABIE_KEY_OVER = "over";
	public static final String ABIE_KEY_SHIFT = "shift";
	public static final String ABIE_KEY_ROTATE = "rotate";
	public static final String ABIE_KEY_PAUSE = "pause";
	public static final String ABIE_KEY_LINEAR = "linear";
	public static final String ABIE_KEY_SINE = "sine";
	public static final String ABIE_KEY_COSINE = "cosine";

	public static final String STATES_TAG = "states";
	public static final String STATES_DEFAULTS = "defaults";
	public static final String STATE_NORMAL = "normal";
	public static final String STATE_MODEL_NAME = "model";
	public static final String STATE_MODEL_WEIGHT = "weight";
	public static final String STATE_TEXTURE = "texture";
	public static final String STATE_TEX_SIZE = "size";
	public static final String STATE_ROTATION = "y";
	public static final String STATE_SCALE = "scale";

	public static final String STATES_DIR = "entitystates/";
	public static final String MODELS_DIR = "models/entity/";
	public static final String ANIMS_DIR = "models/entity/animations/";
	public static final String TEXTURES_DIR = "textures/";

	public static final String JSON = ".json";
	public static final String PNG = ".png";
	public static final String ABIESCRIPT = ".abie";

	public static final Pattern IDENTIFIER_REGEX = Pattern.compile("[A-Za-z_][A-Za-z_0-9]*");
	public static final Pattern RESOURCE_LOCATION_REGEX =
		Pattern.compile("(?:(?<domain>[A-Za-z_0-9]+):)?(?<filepath>[A-Za-z_0-9/]+)");

	private Keys() { }
}