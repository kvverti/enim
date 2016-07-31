package kvverti.enim.modelsystem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

import net.minecraft.client.resources.IResource;

import kvverti.enim.Logger;
import kvverti.enim.Util;

public final class Animation {

	public static final Animation NO_OP = new Animation(Arrays.asList(Collections.emptyMap()), Collections.emptySet());

	      //defineName - elementName
	private final Map<String, String> definesElements;
	private final Set<String> defines;
	private final List<Map<String, float[]>> frames;

	private Animation(List<Map<String, float[]>> frames, Set<String> defines) {

		this.frames = frames;
		this.defines = defines;
		this.definesElements = new HashMap<>();
	}

	public int frameCount() {

		return frames.size();
	}

	public String toElementName(String defineName) {

		Objects.requireNonNull(defineName, "Null defineName");
		String result = definesElements.get(defineName);
		if(result == null) throw new IllegalArgumentException("No define: " + defineName);
		return result;
	}

	public Map<String, float[]> frame(int frame) {

		return new HashMap<>(frames.get(frame));
	}

	public int length() {

		return frames.size();
	}

	void validate(Set<String> elements) throws ParserException {

		Util.validate(definesElements.values(),
			elements::contains,
			elem -> new ParserException("Element " + elem + " does not exist"));
	}

	public static Animation compile(IResource file, Map<String, String> defineToElement) {

		AnimationParser parser = new AnimationParser(file);
		try {
			Animation result = parser.parseFrames(parser.parseTokens(parser.parseSource()));
			Util.validate(defineToElement.keySet(),
				result.defines::contains,
				def -> new ParserException("Define not found: " + def));
			result.definesElements.putAll(defineToElement);
			return result;

		} catch(ParserException e) {

			Logger.error(e);
			return NO_OP;
		}
	}

	static Animation compile(List<AnimationFrame> frames, Set<String> defines, int freq) throws SyntaxException {

		assert freq > 0 : "Non-positive frequency";
		List<Map<String, float[]>> frameList = new ArrayList<>();
		Map<String, float[]> frameMap = new HashMap<>();
		defines.forEach(define -> frameMap.put(define, new float[6]));
		for(AnimationFrame frame : frames) {

			Util.validate(frame.anemes(),
				aneme -> defines.contains(aneme.getSpecifiedElement()),
				aneme -> new SyntaxException("Undefined element: " + aneme.getSpecifiedElement()));
			addTrueFrames(frameList, frame, frameMap, freq);
		}
		return new Animation(frameList, defines);
	}

	private static void addTrueFrames(List<Map<String, float[]>> frames,
		AnimationFrame animFrame,
		Map<String, float[]> trueFrame,
		int freq) {

		StateFrameModifier modifier = animFrame.modifier();
		if(modifier != null) {

			int iValue = modifier.getIntModifier();
			switch(modifier.getStatementType()) {

				case REPEAT:
					fillRepeat(frames, animFrame, trueFrame, iValue, freq);
					break;
				case OVER:
					fillOver(frames, animFrame, trueFrame, iValue, freq);
					break;
				default: Util.assertFalse("Invalid statement type");
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
		for(int n = 1; n <= duration; n++) {

			for(StateAneme aneme : animFrame.anemes()) {

				float[] start = prevFrame.get(aneme.getSpecifiedElement());
				float[] stCopy = prevCopy.get(aneme.getSpecifiedElement());
				float[] angles = start.clone();
				float[] rotate = aneme.getAngles();
				float[] shift = aneme.getShifts();
				int atype = aneme.getAngleType();
				int time = aneme.getRelativePeriod();
				switch(aneme.getStatementType()) {

					case ROTATE:
						for(int i = 0; i < 3; i++) {

							if(rotate[i] == 0.0f) continue;
							rotate[i] += stCopy[i];
							angles[i] = selectHelper(atype, stCopy[i], rotate[i], (float) n * time / duration);
						}
						break;
					case SHIFT:
						for(int i = 0; i < 3; i++) {

							if(shift[i] == 0.0f) continue;
							shift[i] += stCopy[i + 3];
							angles[i + 3] = selectHelper(atype, stCopy[i + 3], shift[i], (float) n * time / duration);
						}
						break;
					default: Util.assertFalse("Invalid statement type");
				}
				prevFrame.put(aneme.getSpecifiedElement(), angles);
			}
			frames.add(new HashMap<>(prevFrame));
		}
	}

	private static float selectHelper(int type, float start, float end, float percent) {

		switch(type) {

			case 0: return interpolate(start, end, percent);
			case 1: return interpolateSine(start, end, percent);
			case 2: return interpolateCosine(start, end, percent);
			default: Util.assertFalse("Unknown angle type");
				return 0; //never reached
		}
	}

	public static float interpolate(float start, float end, float percent) {

		return start + percent * (end - start);
	}

	public static float interpolateSine(float start, float end, float percent) {

		return start + (float) Math.sin(percent * 2.0 * Math.PI) * (end - start);
	}

	public static float interpolateCosine(float start, float end, float percent) {

		return start + (float) (1.0 - Math.cos(percent * 2.0 * Math.PI)) * 0.5f * (end - start);
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("Defines:");
		definesElements.forEach((key, value) -> sb.append("\n    " + key + " -> " + value));
		for(int i = 0; i < frames.size(); i++) {

			sb.append("\nFrame " + i);
			frames.get(i).forEach((key, value) -> sb.append("\n    " + key + " set " + Arrays.toString(value)));
		}
		return sb.toString();
	}
}