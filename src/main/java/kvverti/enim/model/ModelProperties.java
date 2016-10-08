package kvverti.enim.model;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import kvverti.enim.Keys;
import kvverti.enim.Vec3f;

/** Class corresponding to the "properties" tag of an entity model */
public class ModelProperties {

	public static final ModelProperties DEFAULT = new ModelProperties();

	@SerializedName(Keys.PROP_NAMETAG_ORIGIN)
	public final float nameplateBase;

	@SerializedName(Keys.PROP_HELD_ITEM_ORIGIN_LEFT)
	public final OriginPoint heldItemLeft;

	@SerializedName(Keys.PROP_HELD_ITEM_ORIGIN_RIGHT)
	public final OriginPoint heldItemRight;

	/** For Json deserialization */
	private ModelProperties() {

		nameplateBase = 0.0f;
		heldItemLeft = OriginPoint.DEFAULT;
		heldItemRight = OriginPoint.DEFAULT;
	}

	@Override
	public String toString() {

		return String.format("ModelProperties { \"%s\": %s, \"%s\": %s, \"%s\": %s }",
			Keys.PROP_NAMETAG_ORIGIN, nameplateBase,
			Keys.PROP_HELD_ITEM_ORIGIN_LEFT, heldItemLeft,
			Keys.PROP_HELD_ITEM_ORIGIN_RIGHT, heldItemRight);
	}

	/** A point in entity space associated with a ModelElement */
	public static class OriginPoint {

		public static final OriginPoint DEFAULT =
			EntityModel.GSON.fromJson("{\"parent\":\"\",\"origin\":[0,0,0]}", OriginPoint.class);

		@SerializedName(Keys.PROP_META_PARENT)
		public final String parent;

		@SerializedName(Keys.PROP_META_ORIGIN)
		public final Vec3f coords;

		/** For Json deserialization */
		private OriginPoint() { parent = ""; coords = Vec3f.ORIGIN; }

		@Override
		public String toString() {

			return String.format("OriginPoint: { \"%s\": \"%s\", \"%s\": %s }",
				Keys.PROP_META_PARENT, parent,
				Keys.PROP_META_ORIGIN, coords);
		}
	}
}