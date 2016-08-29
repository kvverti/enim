package kvverti.enim.modelsystem;

import java.util.Arrays;

import kvverti.enim.Vec3f;

public final class ModelElement {

	public final String name;
	public final int[] uv = { 0, 0 };
	public Vec3f from = Vec3f.ORIGIN;
	public Vec3f to = Vec3f.ORIGIN;
	public Vec3f origin = Vec3f.ORIGIN;
	public Vec3f rotation = Vec3f.ORIGIN;
	public float scale;
	public String parent;
	public boolean translucent;
	public boolean head;

	public ModelElement(String name) {

		this.name = name;
	}

	@Override
	public String toString() {

		return String.format(
			"ModelElement { \"%s\": \"%s\", \"%s\": \"%s\", \"%s\": [%d,%d,%d], \"%s\": [%d,%d,%d], " +
			"\"%s\": [%d,%d], \"%s\": [%d,%d,%d], \"%s\": [%d,%d,%d], \"%s\": %d, \"%s\": %s, \"%s\": %s }",
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

	private void verify(int[] arr) throws SyntaxException {

		for(int n : arr) {

			if(n < 0) throw new SyntaxException("Value must be non-negative: " + n);
		}
	}
}