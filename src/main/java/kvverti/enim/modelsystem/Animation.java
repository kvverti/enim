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
		defines.forEach(define -> frameMap.put(define, new float[3]));
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

			for(StateAneme aneme : animFrame.anemes()) {

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
		definesElements.forEach((key, value) -> sb.append("\n    " + key + " -> " + value));
		for(int i = 0; i < frames.size(); i++) {

			sb.append("\nFrame " + i);
			frames.get(i).forEach((key, value) -> sb.append("\n    " + key + " set " + Arrays.toString(value)));
		}
		return sb.toString();
	}
}