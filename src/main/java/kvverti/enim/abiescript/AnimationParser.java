package kvverti.enim.abiescript;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import net.minecraft.client.resources.IResource;

import kvverti.enim.Vec3f;
import kvverti.enim.Util;
import kvverti.enim.Keys;

public class AnimationParser {

	private IResource file;

	public AnimationParser() { }

	public AbieScript parse(IResource file) {

		this.file = file;
		try { return parseFrames(parseTokens(parseSource())); }
		finally { this.file = null; }
	}

	private List<Token> parseSource() {

		try(InputStream input = file.getInputStream()) {

			List<Token> tokens = new ArrayList<>();
			StringBuilder s = new StringBuilder();
			int charValue;

			while((charValue = input.read()) != -1) {

				if(charValue == '#') {

					//skip the rest of the line
					while((charValue = input.read()) != '\n' && charValue != -1);

				} else if(Character.isWhitespace(charValue)) {

					if(s.length() != 0) {

						tokens.add(Token.compile(s.toString()));
						s.delete(0, s.length());
					}

				} else s.append((char) charValue);
			}
			return tokens;

		} catch(IOException e) {

			throw new AbieIOException(file + ": Could not read input stream", e);
		}
	}

	//so we don't create two instances for every statement
	private static final Token[] TOKEN_ARR = new Token[0];

	private List<Statement> parseTokens(List<Token> tokens) {

		List<Statement> statements = new ArrayList<>();
		for(int i = 0; i < tokens.size(); ) {

			if(tokens.get(i).getTokenType() == TokenType.COMMAND) {

				try {
					String cmdName = tokens.get(i).getValue();
					Token[] arr = tokens.subList(++i, i += tillNextCmd(tokens, i)).toArray(TOKEN_ARR);
					statements.add(Statement.compile(cmdName, arr));

				} catch(IndexOutOfBoundsException e) {

					throw new AbieSyntaxException(file + ": Reached end of file while parsing");
				}

			} else throw new AbieSyntaxException(file + ": Expected command at token " + i + ": " + tokens.get(i));
		}
		return statements;
	}

	/* length from start until the next COMMAND token */
	private int tillNextCmd(List<Token> tokens, int start) {

		int res = 0;
		while(start + res < tokens.size() && tokens.get(start + res).getTokenType() != TokenType.COMMAND)
			res++;
		return res;
	}

	private int addPause(List<Statement> statements, int index, List<AnimationFrame> frames) {

		StatePause pause = (StatePause) statements.get(index++);
		frames.add(AnimationFrame.compilePause(pause.getPauseDuration()));
		return index;
	}

	private int addFrame(List<Statement> statements, int index, List<AnimationFrame> frames) {

		try {
			StateFrameModifier currentModifier = statements.get(index) instanceof StateFrameModifier ?
				(StateFrameModifier) statements.get(index++) : null;

			//increment past opening brace then set ELSE set then increment to next,
			//then find the closing brace, if bracketed. Throws IOBException.

			boolean bracketed = statements.get(index).getStatementType() == StatementType.START_FRAME;
			int from = bracketed ? ++index : index++;
			while(bracketed && statements.get(++index).getStatementType() != StatementType.END_FRAME);

			Statement[] currentStates = statements.subList(from, index).toArray(new Statement[index - from]);
			frames.add(AnimationFrame.compile(currentModifier, currentStates));
			//increment past closing brace
			return bracketed ? ++index : index;

		} catch(IndexOutOfBoundsException e) {

			throw new AbieSyntaxException(file + ": Reached end of file while parsing");
		}
	}

	private int addDefine(List<Statement> statements, int index, Set<String> defines) {

		StateDefine state = (StateDefine) statements.get(index++);
		defines.add(state.getDefine());
		return index;
	}

	private AbieScript parseFrames(List<Statement> statements) {

		List<AnimationFrame> frames = new ArrayList<>();
		Set<String> defines = new HashSet<>();
		int freq = 1;
		for(int i = 0; i < statements.size(); ) {

			switch(statements.get(i).getStatementType()) {

				case DEFINITION:
					i = addDefine(statements, i, defines);
					break;
				case FREQUENCY:
					if(freq != 1) throw new AbieSyntaxException("Duplicate freq statement");
					freq = ((StateFreq) statements.get(i++)).getFreq();
					break;
				case PAUSE:
					i = addPause(statements, i, frames);
					break;
				default:
					i = addFrame(statements, i, frames);
					break;
			}
		}
		return compile(frames, defines, freq);
	}

	private AbieScript compile(List<AnimationFrame> frames, Set<String> defines, int freq) {

		assert freq > 0 : "Non-positive frequency";
		List<AbieScript.Frame> frameList = new ArrayList<>();
		Map<String, Vec3f[]> frameMap = new HashMap<>();
		defines.forEach(define -> frameMap.put(define, new Vec3f[] { Vec3f.ORIGIN, Vec3f.ORIGIN }));
		for(AnimationFrame frame : frames) {

			Util.validate(frame.anemes(),
				aneme -> defines.contains(aneme.getSpecifiedElement()),
				aneme -> new AbieSyntaxException("Undefined element: " + aneme.getSpecifiedElement()));
			addTrueFrames(frameList, frame, frameMap, freq);
		}
		return new AbieScript(defines, frameList);
	}

	private void addTrueFrames(List<AbieScript.Frame> frames,
		AnimationFrame animFrame,
		Map<String, Vec3f[]> trueFrame,
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
		} else fillOver(frames, animFrame, trueFrame, 1, freq);
	}

	private static void fillRepeat(List<AbieScript.Frame> frames,
		AnimationFrame animFrame,
		Map<String, Vec3f[]> prevFrame,
		int repeats,
		int freq) {

		while(repeats-- > 0) 
			fillOver(frames, animFrame, prevFrame, 1, freq);
	}

	private static void fillOver(List<AbieScript.Frame> frames,
		AnimationFrame animFrame,
		Map<String, Vec3f[]> prevFrame,
		int duration,
		int freq) {

		duration *= Keys.INTERPOLATION_TICKS * freq;
		Map<String, Vec3f[]> prevCopy = new HashMap<>(prevFrame);
		for(int n = 1; n <= duration; n++) {

			for(StateAneme aneme : animFrame.anemes()) {

				Vec3f[] start = prevFrame.get(aneme.getSpecifiedElement());
				Vec3f[] original = prevCopy.get(aneme.getSpecifiedElement());
				Vec3f[] angles = start.clone();
				Vec3f[] trans = aneme.getTransforms();
				int atype = aneme.getAngleType();
				float time = (float) n * aneme.getRelativePeriod() / duration;
				float x, y, z;
				x = trans[0].x != 0.0f ? selectHelper(atype, original[0].x, original[0].x + trans[0].x, time) : angles[0].x;
				y = trans[0].y != 0.0f ? selectHelper(atype, original[0].y, original[0].y + trans[0].y, time) : angles[0].y;
				z = trans[0].z != 0.0f ? selectHelper(atype, original[0].z, original[0].z + trans[0].z, time) : angles[0].z;
				angles[0] = Vec3f.of(x, y, z);
				x = trans[1].x != 0.0f ? selectHelper(atype, original[1].x, original[1].x + trans[1].x, time) : angles[1].x;
				y = trans[1].y != 0.0f ? selectHelper(atype, original[1].y, original[1].y + trans[1].y, time) : angles[1].y;
				z = trans[1].z != 0.0f ? selectHelper(atype, original[1].z, original[1].z + trans[1].z, time) : angles[1].z;
				angles[1] = Vec3f.of(x, y, z);
				prevFrame.put(aneme.getSpecifiedElement(), angles);
			}
			frames.add(new AbieScript.Frame(new HashMap<>(prevFrame)));
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

	private static float interpolate(float start, float end, float percent) {

		return start + percent * (end - start);
	}

	private static float interpolateSine(float start, float end, float percent) {

		return start + (float) Math.sin(percent * 2.0 * Math.PI) * (end - start);
	}

	private static float interpolateCosine(float start, float end, float percent) {

		return start + (float) (1.0 - Math.cos(percent * 2.0 * Math.PI)) * 0.5f * (end - start);
	}
}