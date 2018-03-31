package kvverti.enim;

import java.util.regex.Pattern;

/** Contains keys used in the ENIM entity model format and the AbieScript specification. */
public final class Keys {

    public static final int INTERPOLATION_TICKS = 3;

    public static final String PROPERTIES_TAG = "properties";
//    public static final String PROP_BOUNDING_BOX = "bounds";
    public static final String PROP_NAMETAG_ORIGIN = "nameplate";
    public static final String PROP_SHADOW_SIZE = "shadowsize";
    public static final String PROP_HELD_ITEM_ORIGIN_RIGHT = "righthand";
    public static final String PROP_HELD_ITEM_ORIGIN_LEFT = "lefthand";
    public static final String PROP_HELMET_ORIGIN = "helmet";
    public static final String PROP_META_PARENT = "element";
    public static final String PROP_META_ORIGIN = "position";
    public static final String PROP_META_ROTATION = "rotation";
    public static final String PROP_META_SCALE = "scale";

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
    public static final String ELEM_PIVOT_POINT = "pivot";
    public static final String ELEM_DEFROT = "rotation";
    public static final String ELEM_SCALE = "scale";
    public static final String ELEM_TRANSLUCENT = "translucent";
    public static final String ELEM_HEAD = "head";
    public static final String ELEM_TINTINDEX = "tintindex";
    public static final String ELEM_MIRRORED = "mirrored";
    public static final String ELEM_TYPE = "type";
    public static final String ELEM_ITEM = "item";
    public static final String ELEM_BLOCKSTATE = "block";
    public static final String ELEM_BLOCKSTATE_BLOCK = "id";
    public static final String ELEM_BLOCKSTATE_STATE = "state";

    public static final String ANIMS_TAG = "animations";
    public static final String ANIM_SCRIPT = "script";
    public static final String ANIM_DEFINES = "with";
    public static final String ANIM_SCALE_WITH_MOVEMENT = "scaled";
    public static final String ANIM_VALUE_SCALE_WEIGHT = "scaling";
    public static final String ANIM_SPEED_SCALE_WEIGHT = "tuning";

    public static final String ABIE_KEY_DEFINE = "define";
    public static final String ABIE_KEY_FREQ = "freq";
    public static final String ABIE_KEY_REPEAT = "repeat";
    public static final String ABIE_KEY_OVER = "over";
    public static final String ABIE_KEY_INIT = "init";
    public static final String ABIE_KEY_SHIFT = "shift";
    public static final String ABIE_KEY_ROTATE = "rotate";
    public static final String ABIE_KEY_PAUSE = "pause";
    public static final String ABIE_KEY_FUNCTION = "function";

    public static final String STATES_TAG = "states";
    public static final String MULTIPART_TAG = "multipart";
    public static final String STATES_DEFAULTS = "defaults";
    public static final String STATE_NORMAL = "normal";
    public static final String STATE_MODEL_NAME = "model";
    public static final String STATE_MODEL_WEIGHT = "weight";
    public static final String STATE_TEXTURE = "texture";
    public static final String STATE_OVERLAY = "overlay";
    public static final String STATE_TEX_SIZE = "size";
    public static final String STATE_ROTATION = "y";
    public static final String STATE_SCALE = "scale";
    public static final String STATE_ARMOR = "armor";
    
    public static final String ARMOR_DEFAULTS = "defaults";
    public static final String ARMOR_MATERIALS = "materials";
    public static final String ARMOR_PARENT = "parent";

    public static final String STATES_DIR = "entitystates/";
    public static final String MODELS_DIR = "models/entity/";
    public static final String ANIMS_DIR = "models/entity/animations/";
    public static final String TEXTURES_DIR = "textures/";
    public static final String COLORS_DIR = "colors/";
    public static final String ARMOR_DIR = "models/armor/";

    public static final String MISSING_DEFINITION = "builtin/missingno";

    public static final String JSON = ".json";
    public static final String PNG = ".png";
    public static final String ABIESCRIPT = ".abie";

    public static final Pattern IDENTIFIER_REGEX = Pattern.compile("[a-z_][a-z_0-9]*");
    public static final Pattern RESOURCE_LOCATION_REGEX =
        Pattern.compile("(?:(?<domain>[a-z_0-9-]+):)?(?<filepath>[a-z_0-9/-]+)");

    private Keys() { }
}