package kvverti.enim.model;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import kvverti.enim.Keys;
import kvverti.enim.Vec3f;

/** Class corresponding to the {@value Keys#PROPERTIES_TAG} tag of an entity model */
public class ModelProperties {

	public static final ModelProperties DEFAULT = new ModelProperties();

	@SerializedName(Keys.PROP_NAMETAG_ORIGIN)
	private final float nameplateBase;

	@SerializedName(Keys.PROP_HELD_ITEM_ORIGIN_LEFT)
	private final OriginPoint heldItemLeft;

	@SerializedName(Keys.PROP_HELD_ITEM_ORIGIN_RIGHT)
	private final OriginPoint heldItemRight;

	/** For Json deserialization */
	private ModelProperties() {

		nameplateBase = 0.0f;
		heldItemLeft = OriginPoint.DEFAULT;
		heldItemRight = OriginPoint.DEFAULT;
	}

	/**
	 * The height at which the entity's nameplate should render when using this model. This is the distance from the ground to the bottom
	 * of the nameplate in pixel units.
	 */
	public float nameplate() { return nameplateBase; }

	/**
	 * The position of the entity's left hand. Items held in the left hand will be rendered at this position and will move with the
	 * element specified as the parent.
	 */
	public OriginPoint leftHand() { return heldItemLeft; }

	/**
	 * The position of the entity's right hand. Items held in the right hand will be rendered at this position and will move with the
	 * element specified as the parent.
	 */
	public OriginPoint rightHand() { return heldItemRight; }

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