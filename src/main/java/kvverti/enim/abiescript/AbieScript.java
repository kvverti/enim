package kvverti.enim.abiescript;

import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableList;

import kvverti.enim.Keys;
import kvverti.enim.Vec3f;

import static java.util.stream.Collectors.joining;

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

		private final Map<String, Vec3f[]> frame;

		/** For use by AnimationParser */
		Frame(Map<String, Vec3f[]> data) { frame = data; }

		public Vec3f[] getTransforms(String define) {

			Vec3f[] res = frame.get(define);
			if(res == null) throw new IllegalArgumentException("Not a define: " + define);
			return res;
		}

		@Override
		public String toString() {

			return "[" + frame.entrySet().stream()
				.map(entry -> entry.getKey() + "=" + Arrays.toString(entry.getValue()))
				.collect(joining(", "))
				+ "]";
		}
	}
}