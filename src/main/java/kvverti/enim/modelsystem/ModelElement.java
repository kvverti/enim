package kvverti.enim.modelsystem;

import java.util.Arrays;

public final class ModelElement {

	private final String name;
	private final int[] coords;
	private final float[] from;
	private final float[] to;
	private final float[] rotationPoint;
	private final float[] defaultRotation;
	private final float scale;
	private final String parent;
	private final boolean translucent;
	private final boolean head;

	private ModelElement(Builder b) {

		name = b.name;
		parent = b.parent;
		coords = b.coords;
		from = b.from;
		to = b.to;
		rotationPoint = b.rotationPoint;
		defaultRotation = b.defrots;
		scale = b.scale;
		translucent = b.translucent;
		head = b.head;
	}

	public String name() {

		return name;
	}

	public String parent() {

		return parent;
	}

	public int[] texCoords() {

		return coords.clone();
	}

	public float[] from() {

		return from.clone();
	}

	public float[] to() {

		return to.clone();
	}

	public float[] rotationPoint() {

		return rotationPoint.clone();
	}

	public float[] defaultRotation() {

		return defaultRotation.clone();
	}

	public float scale() {

		return scale;
	}

	public boolean isTranslucent() {

		return translucent;
	}

	public boolean isHead() {

		return head;
	}

	@Override
	public String toString() {

		return String.format(
			"ModelElement { \"%s\": \"%s\", \"%s\": \"%s\", \"%s\": [%d,%d,%d], \"%s\": [%d,%d,%d], " +
			"\"%s\": [%d,%d], \"%s\": [%d,%d,%d], \"%s\": [%d,%d,%d], \"%s\": %d, \"%s\": %s, \"%s\": %s }",
			Keys.ELEM_NAME, name,
			Keys.ELEM_PARENT, parent,
			Keys.ELEM_FROM, from[0], from[1], from[2],
			Keys.ELEM_TO, to[0], to[1], to[2],
			Keys.ELEM_TEXCOORDS, coords[0], coords[1],
			Keys.ELEM_ROTPOINT, rotationPoint[0], rotationPoint[1], rotationPoint[2],
			Keys.ELEM_DEFROT, defaultRotation[0], defaultRotation[1], defaultRotation[2],
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

	static final class Builder {

		private String name = null;
		private int[] coords = new int[2];
		private float[] from = new float[3];
		private float[] to = new float[3];
		private float[] rotationPoint = new float[3];
		private float[] defrots = new float[3];
		private float scale = 1.0f;
		private String parent = null;
		private boolean translucent = false;
		private boolean head = false;

		public Builder() { }

		public Builder(ModelElement elem) {

			name = elem.name;
			coords = elem.coords.clone();
			from = elem.from.clone();
			to = elem.to.clone();
			rotationPoint = elem.rotationPoint.clone();
			defrots = elem.defaultRotation.clone();
			scale = elem.scale;
			parent = elem.parent;
			translucent = elem.translucent;
			head = elem.head;
		}

		public Builder setName(String name) {

			this.name = name;
			return this;
		}

		public Builder setTexCoords(int x, int y) {

			coords[0] = x;
			coords[1] = y;
			return this;
		}

		public Builder setFrom(float x, float y, float z) {

			from[0] = x;
			from[1] = y;
			from[2] = z;
			return this;
		}

		public Builder setTo(float x, float y, float z) {

			to[0] = x;
			to[1] = y;
			to[2] = z;
			return this;
		}

		public Builder setRotationPoint(float x, float y, float z) {

			rotationPoint[0] = x;
			rotationPoint[1] = y;
			rotationPoint[2] = z;
			return this;
		}

		public Builder setDefaultRotation(float x, float y, float z) {

			defrots[0] = x;
			defrots[1] = y;
			defrots[2] = z;
			return this;
		}

		public Builder setScale(float scale) {

			this.scale = scale;
			return this;
		}

		public Builder setParent(String parent) {

			this.parent = parent;
			return this;
		}

		public Builder setTranslucent(boolean value) {

			translucent = value;
			return this;
		}

		public Builder setHead(boolean value) {

			head = value;
			return this;
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

		public ModelElement build() throws SyntaxException {

			verify(name);
			verify(coords);
			return new ModelElement(this);
		}
	}
}