package kvverti.enim.modelsystem;

import java.util.Arrays;

public final class ModelElement {

	private final String name;
	private final int[] coords;
	private final int[] from;
	private final int[] to;
	private final int[] rotationPoint;
	private final int[] defaultRotation;
	private final String parent;

	private ModelElement(String name,
		String parent,
		int[] coords,
		int[] dims,
		int[] offs,
		int[] rots,
		int[] defRots) {

		this.name = name;
		this.parent = parent;
		this.coords = coords;
		from = dims;
		to = offs;
		rotationPoint = rots;
		defaultRotation = defRots;
	}

	public String getName() {

		return name;
	}

	public String getParent() {

		return parent;
	}

	public int[] getTexCoords() {

		return coords;
	}

	public int[] getFrom() {

		return from;
	}

	public int[] getTo() {

		return to;
	}

	public int[] getRotationPoint() {

		return rotationPoint;
	}

	public int[] getDefaultRotation() {

		return defaultRotation;
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

	public static final class Builder {

		private String name = null;
		private int[] coords = new int[2];
		private int[] from = new int[3];
		private int[] to = new int[3];
		private int[] rotationPoint = new int[3];
		private int[] defrots = new int[3];
		private String parent = null;

		public Builder setName(String name) {

			this.name = name;

			return this;
		}

		public Builder setTexCoords(int x, int y) {

			coords[0] = x;
			coords[1] = y;

			return this;
		}

		public Builder setFrom(int x, int y, int z) {

			from[0] = x;
			from[1] = y;
			from[2] = z;

			return this;
		}

		public Builder setTo(int x, int y, int z) {

			to[0] = x;
			to[1] = y;
			to[2] = z;

			return this;
		}

		public Builder setRotationPoint(int x, int y, int z) {

			rotationPoint[0] = x;
			rotationPoint[1] = y;
			rotationPoint[2] = z;

			return this;
		}

		public Builder setDefaultRotation(int x, int y, int z) {

			defrots[0] = x;
			defrots[1] = y;
			defrots[2] = z;

			return this;
		}

		public Builder setParent(String parent) {

			this.parent = parent;

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

			return new ModelElement(name, parent, coords, from, to, rotationPoint, defrots);
		}
	}
}