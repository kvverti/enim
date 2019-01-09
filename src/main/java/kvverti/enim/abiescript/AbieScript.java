package kvverti.enim.abiescript;

import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableList;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import kvverti.enim.Keys;
import kvverti.enim.Vec3f;

import static kvverti.enim.entity.Entities.toRadians;

public class AbieScript {

    private final ImmutableSet<String> defines;
    private final ImmutableList<Frame> frames;

    /** For use by AnimationParser */
    AbieScript(Set<String> defines, List<Frame> frames) {

        this.defines = ImmutableSet.copyOf(defines);
        this.frames = ImmutableList.copyOf(frames);
    }

    public ImmutableSet<String> defines() {

        return defines;
    }

    public int frameCount() {

        return frames.size() / Keys.INTERPOLATION_TICKS;
    }

    public Frame frame(int index, float partial) {

        //intentional integer overflow
        int idx = index * Keys.INTERPOLATION_TICKS + (int) (partial * Keys.INTERPOLATION_TICKS);
        assert idx >= 0 && idx < frames.size() : idx;
        return frames.get(idx);
    }

    @Override
    public String toString() {

        return "defines: " + defines + ", frames: " + frames;
    }

    public static class Frame {

        private final Map<String, Transform> transforms;

        /** For use by AnimationParser */
        Frame(Map<String, Vec3f[]> data) {
            transforms = new HashMap<>();
            for(Map.Entry<String, Vec3f[]> e : data.entrySet()) {
                Vec3f rotAngles = e.getValue()[0];
                Matrix4f matrix = new Matrix4f();
                Vector3f axis = new Vector3f();
                axis.set(0.0f, 0.0f, 1.0f);
                matrix.rotate(+toRadians(rotAngles.z), axis);
                axis.set(0.0f, 1.0f, 0.0f);
                matrix.rotate(+toRadians(rotAngles.y), axis);
                axis.set(1.0f, 0.0f, 0.0f);
                matrix.rotate(-toRadians(rotAngles.x), axis);
                Quaternion q = new Quaternion().setFromMatrix(matrix);
                // extract the axis-angle
                float f = (float) Math.sqrt(1 - q.w * q.w);
                Vec3f ax;
                if(f == 0.0f) {
                    ax = Vec3f.UNIT_X;
                } else {
                    ax = Vec3f.of(q.x / f, q.y / f, q.z / f);
                }
                float th = 2 * (float)Math.acos(q.w);
                Vec3f translation = e.getValue()[1];
                transforms.put(e.getKey(), new Transform(translation, ax, th));
            }
        }

        public Transform getAffineTransform(String define) {
            Transform res = transforms.get(define);
            if(res == null) throw new IllegalArgumentException("Not a define: " + define);
            return res;
        }

        public static class Transform {

            public final Vec3f translation;
            public final Vec3f rotationAxis;
            public final float rotationAngle;

            Transform(Vec3f t, Vec3f rx, float ra) {
                translation = t;
                rotationAxis = rx;
                rotationAngle = ra;
            }
        }
    }
}
