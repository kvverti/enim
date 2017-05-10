package kvverti.enim.model;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import kvverti.enim.Keys;
import kvverti.enim.Vec3f;

/** Class representing a model element in the "elements" tag */
public final class ModelElement {

	private static int nameIdx = 0;

	@SerializedName(Keys.ELEM_NAME)
	private String name = "element" + nameIdx++;

	@SerializedName(Keys.ELEM_TEXCOORDS)
	private final int[] uv = { 0, 0 };

	@SerializedName(Keys.ELEM_FROM)
	private Vec3f from = Vec3f.ORIGIN;

	@SerializedName(Keys.ELEM_TO)
	private Vec3f to = Vec3f.ORIGIN;

	@SerializedName(Keys.ELEM_ROTPOINT)
	private Vec3f origin = Vec3f.ORIGIN;

	@SerializedName(Keys.ELEM_PIVOT_POINT)
	private Vec3f pivot = null; //defaults to value of origin

	@SerializedName(Keys.ELEM_DEFROT)
	private Vec3f rotation = Vec3f.ORIGIN;

	@SerializedName(Keys.ELEM_SCALE)
	private float scale = 1.0f;

	@SerializedName(Keys.ELEM_PARENT)
	private String parent = null;

	@SerializedName(Keys.ELEM_TRANSLUCENT)
	private boolean translucent = false;

	@SerializedName(Keys.ELEM_HEAD)
	private boolean head = false;

	@SerializedName(Keys.ELEM_TINTINDEX)
	private int tintIndex = -1;

	/** For Json deserialization */
	private ModelElement() { }

	/** The name of this element. */
	public String name() { return name; }

	/** The parent element to this element, or null if there is no parent. */
	public String parent() { return parent; }

	/** The lower vertex of this cuboid element. */
	public Vec3f from() { return from; }

	/** The upper vertex of this cuboid element. */
	public Vec3f to() { return to; }

	/** The texture coordinates for this element. */
	public int[] uv() { return uv.clone(); }

	/** The origin of transformation for this element. */
	public Vec3f origin() { return origin; }

	/** The rotation origin of this element. Defaults to the value of {@link #origin()}. */
	public Vec3f pivot() { return pivot != null ? pivot : origin; }

	/** The rotation of this element about its pivot. */
	public Vec3f rotation() { return rotation; }

	/** The scale of this element relative to its origin. */
	public float scale() { return scale; }

	/** Whether this element supports partial alpha. */
	public boolean isTranslucent() { return translucent; }

	/** Whether this element represents a head. */
	public boolean isHead() { return head; }

	/** The index of the colormap to multiply onto this element. Returns -1 if not colored. */
	public int tintIndex() { return tintIndex; }

	void applyOverride(Override value) {

		if(value == null) return;
		rotation = value.rotation;
		scale = value.scale;
	}

	@java.lang.Override
	public String toString() {

		return String.format(
			"ModelElement { \"%s\": \"%s\", \"%s\": \"%s\", \"%s\": [%f,%f,%f], \"%s\": [%f,%f,%f], " +
			"\"%s\": [%d,%d], \"%s\": [%f,%f,%f], \"%s\": [%f,%f,%f], \"%s\": %f, \"%s\": %s, \"%s\": %s }",
			Keys.ELEM_NAME, name,
			Keys.ELEM_PARENT, parent,
			Keys.ELEM_FROM, from.x, from.y, from.z,
			Keys.ELEM_TO, to.x, to.y, to.z,
			Keys.ELEM_TEXCOORDS, uv[0], uv[1],
			Keys.ELEM_ROTPOINT, origin.x, origin.y, origin.z,
			Keys.ELEM_DEFROT, rotation.x, rotation.y, rotation.z,
			Keys.ELEM_SCALE, scale,
			Keys.ELEM_TRANSLUCENT, translucent,
			Keys.ELEM_HEAD, head);
	}

	@java.lang.Override
	public boolean equals(Object o) {

		return o != null && o.getClass() == ModelElement.class && ((ModelElement) o).name.equals(this.name);
	}

	@java.lang.Override
	public int hashCode() {

		return name.hashCode();
	}

	public void verify() {

		verifyName(name);
		verifyCoords(uv);
	}

	private void verifyName(String name) {

		boolean flag;
		flag = name != null && name.length() > 0 && Keys.IDENTIFIER_REGEX.matcher(name).matches();
		if(!flag) throw new JsonParseException("Invalid name: " + name);
	}

	private void verifyCoords(int[] coords) {

		if(coords.length != 2) throw new JsonParseException("UV length must be 2");
		for(int n : coords)
			if(n < 0) throw new JsonParseException("Value must be non-negative: " + n);
	}

	/** Class representing an entry in the "overrides" tag */
	static class Override {

		@SerializedName(Keys.ELEM_DEFROT)
		Vec3f rotation = Vec3f.ORIGIN;

		@SerializedName(Keys.ELEM_SCALE)
		float scale = 1.0f;

		/** For Json deserialization */
		private Override() { }
	}
}