package kvverti.enim;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * An immutable vector of three {@code float} values. Three related {@code float} values are used so often in rendering code
 * that it makes sense for this logical grouping of them to exist. This is not a mathematical vector, it is literally
 * three {@code float}s.
 */
public final class Vec3f implements Iterable<Float> {

	public static final Vec3f ORIGIN = new Vec3f(0.0f, 0.0f, 0.0f);
	public static final Vec3f UNIT_X = new Vec3f(1.0f, 0.0f, 0.0f);
	public static final Vec3f UNIT_Y = new Vec3f(0.0f, 1.0f, 0.0f);
	public static final Vec3f UNIT_Z = new Vec3f(0.0f, 0.0f, 1.0f);
	public static final Vec3f IDENTITY = new Vec3f(1.0f, 1.0f, 1.0f);
	public static final Vec3f INFINITY = new Vec3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
	public static final Vec3f NaN = new Vec3f(Float.NaN, Float.NaN, Float.NaN);

	public final float x;
	public final float y;
	public final float z;

	private Vec3f(float x, float y, float z) {

		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static Vec3f of(float x, float y, float z) {

		return equals(ORIGIN, x, y, z) ? ORIGIN
			: equals(IDENTITY, x, y, z) ? IDENTITY
			: new Vec3f(x, y, z);
	}

	private static boolean equals(Vec3f vec, float x, float y, float z) {

		return equalsOrNaN(vec.x, x) && equalsOrNaN(vec.y, y) && equalsOrNaN(vec.z, z);
	}

	private static boolean equalsOrNaN(float f1, float f2) {

		return Float.isNaN(f1) ? Float.isNaN(f2) : f1 == f2;
	}

	@Override
	public Iterator<Float> iterator() {

		return new Iterator<Float>() {

			private byte index = 0;

			@Override
			public boolean hasNext() { return index < 3; }

			@Override
			public Float next() {

				switch(index++) {

					case 0: return x;
					case 1: return y;
					case 2: return z;
					default: throw new NoSuchElementException();
				}
			}
		};
	}

	@Override
	public String toString() {

		return String.format("[%d, %d, %d]", x, y, z);
	}

	@Override
	public int hashCode() {

		return Objects.hash(x, y, z);
	}

	@Override
	public boolean equals(Object obj) {

		return obj instanceof Vec3f && equals((Vec3f) obj, x, y, z);
	}
}