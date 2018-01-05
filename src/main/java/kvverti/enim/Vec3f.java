package kvverti.enim;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * An immutable vector of three {@code float} values. Three related {@code float} values are used so often in rendering code
 * that it makes sense for this logical grouping of them to exist. This is not a mathematical vector, it is literally
 * three {@code float}s.
 */
public final class Vec3f {

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

    /** Private constructor */
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

    public static Vec3f of(float[] values) {

        return of(values[0], values[1], values[2]);
    }

    private static boolean equals(Vec3f vec, float x, float y, float z) {

        return equalsOrNaN(vec.x, x) && equalsOrNaN(vec.y, y) && equalsOrNaN(vec.z, z);
    }

    private static boolean equalsOrNaN(float f1, float f2) {

        return Float.isNaN(f1) ? Float.isNaN(f2) : f1 == f2;
    }

    /** Returns the result of adding the given Vec3f to this Vec3f. */
    public Vec3f add(Vec3f other) {

        return Vec3f.of(x + other.x, y + other.y, z + other.z);
    }

    /** Returns the result of scaling this Vec3f by the given amount. */
    public Vec3f scale(float scalar) {

        return Vec3f.of(x * scalar, y * scalar, z * scalar);
    }

    /** Returns the result of scaling this Vec3f by the corresponding elements of the given Vec3f. */
    public Vec3f scale(Vec3f other) {

        return Vec3f.of(x * other.x, y * other.y, z * other.z);
    }

    /** Returns the result of applying the given mapping function to this Vec3f. */
    public Vec3f map(DoubleUnaryOperator mapper) {

        return Vec3f.of((float) mapper.applyAsDouble(x), (float) mapper.applyAsDouble(y), (float) mapper.applyAsDouble(z));
    }

    /** Returns the result of combining this Vec3f with the given Vec3f, using the given combiner. */
    public Vec3f combine(Vec3f other, DoubleBinaryOperator mapper) {

        return Vec3f.of(
            (float) mapper.applyAsDouble(x, other.x),
            (float) mapper.applyAsDouble(y, other.y),
            (float) mapper.applyAsDouble(z, other.z));
    }

    /** Returns the result of interpolating the given amount between this Vec3f and the given Vec3f. */
    public Vec3f interpolate(Vec3f to, float percent) {

        float oneMinusPercent = 1.0f - percent;
        return Vec3f.of(
            x * oneMinusPercent + to.x * percent,
            y * oneMinusPercent + to.y * percent,
            z * oneMinusPercent + to.z * percent);
    }

    /** Returns the value of this Vec3f as a float[] of length 3. */
    public float[] toFloatArray() {

        return new float[] { x, y, z };
    }

    @Override
    public String toString() {

        return String.format("[%s, %s, %s]", x, y, z);
    }

    @Override
    public int hashCode() {

        return Objects.hash(x, y, z);
    }

    @Override
    public boolean equals(Object obj) {

        return (this == obj) || (obj instanceof Vec3f && equals((Vec3f) obj, x, y, z));
    }

    /** Json adapter for the {@link Vec3f} class. Vec3fs are serialized to a JsonArray. */
    public static class Adapter extends TypeAdapter<Vec3f> {

        @Override
        public Vec3f read(JsonReader in) throws IOException {

            in.beginArray();
            float[] coords = new float[3];
            for(int i = 0; in.hasNext(); i++)
                if(i < 3) coords[i] = (float) in.nextDouble();
                else in.skipValue();
            in.endArray();
            return Vec3f.of(coords[0], coords[1], coords[2]);
        }

        @Override
        public void write(JsonWriter out, Vec3f value) throws IOException {

            out.beginArray();
            out.value(value.x).value(value.y).value(value.z);
            out.endArray();
        }
    }
}