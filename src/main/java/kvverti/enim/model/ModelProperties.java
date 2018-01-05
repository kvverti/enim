package kvverti.enim.model;

import com.google.gson.annotations.SerializedName;

import kvverti.enim.Keys;
import kvverti.enim.Vec3f;

/** Class corresponding to the {@value Keys#PROPERTIES_TAG} tag of an entity model */
public class ModelProperties {

    public static final ModelProperties DEFAULT = new ModelProperties();

    @SerializedName(Keys.PROP_NAMETAG_ORIGIN)
    private final float nameplateBase;

    @SerializedName(Keys.PROP_SHADOW_SIZE)
    private final float shadowSize;

    @SerializedName(Keys.PROP_HELD_ITEM_ORIGIN_LEFT)
    private final OriginPoint heldItemLeft;

    @SerializedName(Keys.PROP_HELD_ITEM_ORIGIN_RIGHT)
    private final OriginPoint heldItemRight;

    @SerializedName(Keys.PROP_HELMET_ORIGIN)
    private final OriginPoint helmet;

    /** For Json deserialization */
    private ModelProperties() {

        nameplateBase = 0.0f;
        shadowSize = 0.0f;
        heldItemLeft = null;
        heldItemRight = null;
        helmet = null;
    }

    /**
     * The height at which the entity's nameplate should render when using this model. This is the distance from the ground to the bottom
     * of the nameplate in pixel units.
     */
    public float nameplate() { return nameplateBase; }

    /**
     * The radius of the model's shadow in pixel units. Living entities will render the shadow at this size.
     */
    public float shadowSize() { return shadowSize; }

    /**
     * The position of the entity's left hand. Items held in the left hand will be rendered at this position and will move with the
     * element specified as the parent. Returns null if this model has no left hand.
     */
    public OriginPoint leftHand() { return heldItemLeft; }

    /**
     * The position of the entity's right hand. Items held in the right hand will be rendered at this position and will move with the
     * element specified as the parent. Returns null if this model has no right hand.
     */
    public OriginPoint rightHand() { return heldItemRight; }

    /**
     * The position of the entity's head, for helmet rendering purposes. Items worn on the head will be
     * rendered at this position and will move with the element specified as the parent.
     * Returns null if this model has no helmet.
     */
    public OriginPoint helmet() { return helmet; }

    @Override
    public String toString() {

        return String.format("ModelProperties { \"%s\": %s, \"%s\": %s, \"%s\": %s }",
            Keys.PROP_NAMETAG_ORIGIN, nameplateBase,
            Keys.PROP_HELD_ITEM_ORIGIN_LEFT, heldItemLeft,
            Keys.PROP_HELD_ITEM_ORIGIN_RIGHT, heldItemRight);
    }

    /** A point in entity space associated with a ModelElement, with optional rotation. */
    public static class OriginPoint {

        public static final OriginPoint DEFAULT =
            EntityModel.GSON.fromJson("{\"element\":\"\",\"rotation\":[0,0,0],\"position\":[0,0,0]}", OriginPoint.class);

        @SerializedName(Keys.PROP_META_PARENT)
        private final String parent;

        @SerializedName(Keys.PROP_META_ORIGIN)
        private final Vec3f coords;

        @SerializedName(Keys.PROP_META_ROTATION)
        private final Vec3f rotation;

        @SerializedName(Keys.PROP_META_SCALE)
        private final ScaleProperty scale;

        /** For Json deserialization */
        private OriginPoint() { parent = ""; coords = Vec3f.ORIGIN; rotation = Vec3f.ORIGIN; scale = ScaleProperty.ONE; }

        /** Returns the parent element for this point, or the empty string if there is no parent element. */
        public String parent() { return parent; }

        /** Returns the position of this point relative to the origin of the block space. */
        public Vec3f coords() { return coords; }

        /** Returns the rotation of this point. */
        public Vec3f rotation() { return rotation; }

        /** Returns the scale of this point. */
        public Vec3f scale() { return scale.value; }

        @Override
        public String toString() {

            return String.format("OriginPoint: { \"%s\": \"%s\", \"%s\": %s }",
                Keys.PROP_META_PARENT, parent,
                Keys.PROP_META_ORIGIN, coords);
        }
    }
}