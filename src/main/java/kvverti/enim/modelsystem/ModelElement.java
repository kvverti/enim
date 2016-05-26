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

	private ModelElement(String name,
		String parent,
		int[] coords,
		float[] dims,
		float[] offs,
		float[] rots,
		float[] defRots,
		float scales,
		boolean lucent) {

		this.name = name;
		this.parent = parent;
		this.coords = coords;
		from = dims;
		to = offs;
		rotationPoint = rots;
		defaultRotation = defRots;
		scale = scales;
		translucent = lucent;
	}

	public String name() {

		return name;
	}

	public String parent() {

		return parent;
	}

	public int[] texCoords() {

		return coords;
	}

	public float[] from() {

		return from;
	}

	public float[] to() {

		return to;
	}

	public float[] rotationPoint() {

		return rotationPoint;
	}

	public float[] defaultRotation() {

		return defaultRotation;
	}

	public float scale() {

		return scale;
	}

	public boolean isTranslucent() {

		return translucent;
	}

	@Override
	public String toString() {

		return "ModelElement[Name: " + name + ", Parent: " + parent + ", TexCoords: " + Arrays.toString(coords)
			+ ", From: " + Arrays.toString(from) + ", To: " + Arrays.toString(to)
			+ ", Rotation Point: " + Arrays.toString(rotationPoint)
			+ ", Default Rotation: " + Arrays.toString(defaultRotation) + "]";
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

			return new ModelElement(name, parent, coords, from, to, rotationPoint, defrots, scale, translucent);
		}
	}
}