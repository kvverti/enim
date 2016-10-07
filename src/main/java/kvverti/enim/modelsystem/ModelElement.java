package kvverti.enim.modelsystem;

import java.util.Arrays;
import java.io.IOException;

import com.google.gson.annotations.SerializedName;

import kvverti.enim.Vec3f;

public final class ModelElement {

	@SerializedName(Keys.ELEM_NAME)
	public final String name;

	@SerializedName(Keys.ELEM_TEXCOORDS)
	public final int[] uv = { 0, 0 };

	@SerializedName(Keys.ELEM_FROM)
	public Vec3f from = Vec3f.ORIGIN;

	@SerializedName(Keys.ELEM_TO)
	public Vec3f to = Vec3f.ORIGIN;

	@SerializedName(Keys.ELEM_ROTPOINT)
	public Vec3f origin = Vec3f.ORIGIN;

	@SerializedName(Keys.ELEM_DEFROT)
	public Vec3f rotation = Vec3f.ORIGIN;

	@SerializedName(Keys.ELEM_SCALE)
	public float scale = 1.0f;

	@SerializedName(Keys.ELEM_PARENT)
	public String parent = null;

	@SerializedName(Keys.ELEM_TRANSLUCENT)
	public boolean translucent = false;

	@SerializedName(Keys.ELEM_HEAD)
	public boolean head = false;

	/** For Json deserialization */
	private ModelElement() { this(""); }

	public ModelElement(String name) {

		this.name = name;
	}

	@Override
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

	@Override
	public boolean equals(Object o) {

		return o != null && o.getClass() == ModelElement.class && ((ModelElement) o).name.equals(this.name);
	}

	@Override
	public int hashCode() {

		return name.hashCode();
	}

	public void verify() throws SyntaxException {

		verify(name);
		verify(uv);
	}

	private void verify(String name) throws SyntaxException {

		boolean flag;
		flag = name != null && name.length() > 0 && Keys.IDENTIFIER_REGEX.matcher(name).matches();
		if(!flag) throw new SyntaxException("Invalid name: " + name);
	}

	private void verify(int[] coords) throws SyntaxException {

		if(coords.length != 2) throw new SyntaxException("UV length must be 2");
		for(int n : coords) {

			if(n < 0) throw new SyntaxException("Value must be non-negative: " + n);
		}
	}
}