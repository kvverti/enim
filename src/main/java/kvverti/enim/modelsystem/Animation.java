package kvverti.enim.modelsystem;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class Animation {

	private final Map<String, String> defines;
	private final List<Map<String, float[]>> frames;

	private Animation(List<Map<String, float[]>> frames, Map<String, String> defines) {

		this.frames = frames;
		this.defines = defines;
	}

	public int frameCount() {

		return frames.size();
	}

	public String toElementName(String defineName) {

		if(defineName == null) throw new NullPointerException("Null defineName");
		String result = defines.get(defineName);
		if(result == null) throw new IllegalArgumentException("No define: " + defineName);
		return result;
	}

	public Map<String, float[]> getFrame(int frame) {

		return new HashMap<>(frames.get(frame));
	}

	public int length() {

		return frames.size();
	}

	static Animation compile(List<AnimationFrame> frames, Set<String> defines, int freq) throws SyntaxException {

		assert freq > 0 : "Non-positive frequency";
		Map<String, String> defMap = new HashMap<>();
		List<Map<String, float[]>> frameList = new ArrayList<>();
		Map<String, float[]> frameMap = new HashMap<>();
		for(String define : defines) {

			frameMap.put(define, new float[3]);
			defMap.put(define, null);
		}
		for(AnimationFrame frame : frames) {

			for(StateAneme s : frame.getAnemes()) {

				String name = s.getSpecifiedElement();
				if(!defines.contains(name))
					throw new SyntaxException("Undefined element: " + name);
			}
			addTrueFrames(frameList, frame, frameMap, freq);
		}
		return new Animation(frameList, defMap);
	}

	private static void addTrueFrames(List<Map<String, float[]>> frames,
		AnimationFrame animFrame,
		Map<String, float[]> trueFrame,
		int freq) {

		StateFrameModifier modifier = animFrame.getModifier();
		if(modifier != null) {

			int iValue = modifier.getIntModifier();
			switch(modifier.getStatementType()) {

				case REPEAT:
					fillRepeat(frames, animFrame, trueFrame, iValue, freq);
					break;

				case OVER:
					fillOver(frames, animFrame, trueFrame, iValue, freq);
					break;

				default: assert false : "Switch error";
			}

		} else {

			fillOver(frames, animFrame, trueFrame, 1, freq);
		}
	}

	private static void fillRepeat(List<Map<String, float[]>> frames,
		AnimationFrame animFrame,
		Map<String, float[]> prevFrame,
		int repeats,
		int freq) {

		while(repeats-- > 0) {

			fillOver(frames, animFrame, prevFrame, 1, freq);
		}
	}

	private static void fillOver(List<Map<String, float[]>> frames,
		AnimationFrame animFrame,
		Map<String, float[]> prevFrame,
		int duration,
		int freq) {

		duration *= freq;
		Map<String, float[]> prevCopy = new HashMap<>(prevFrame);
		while(duration > 0) {

			for(StateAneme aneme : animFrame.getAnemes()) {

				float[] start = prevFrame.get(aneme.getSpecifiedElement());
				float[] stCopy = prevCopy.get(aneme.getSpecifiedElement());
				float[] end = aneme.getAngles();
				float[] angles = new float[3];
				for(int i = 0; i < 3; i++) {

					if(aneme.getStatementType() == StatementType.ROTATE) {

						end[i] += stCopy[i];

					} else if(Float.valueOf(end[i]).equals(Float.valueOf(-0.0f))) {

						end[i] = start[i];
					}
					angles[i] = interpolate(start[i], end[i], 1.0f / duration);
				}
				prevFrame.put(aneme.getSpecifiedElement(), angles);
			}
			frames.add(new HashMap<>(prevFrame));
			duration--;
		}
	}

	public static float interpolate(float start, float end, float percent) {

		float result = start + percent * (end - start);
	//	if(result > 180.0f) result -= 360.0f;
	//	else if(result <= -180.0f) result += 360.0f;
		return result;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("Defines:");
		for(Map.Entry<String, String> entry : defines.entrySet()) {

			sb.append("\n    " + entry.getKey() + " -> " + entry.getValue());
		}
		for(int i = 0; i < frames.size(); i++) {

			sb.append("\nFrame " + i);
			for(Map.Entry<String, float[]> entry : frames.get(i).entrySet()) {

				sb.append("\n    " + entry.getKey() + " set " + Arrays.toString(entry.getValue()));
			}
		}
		return sb.toString();
	}
}