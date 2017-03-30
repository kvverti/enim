package kvverti.enim.abiescript;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.Vec3f;
import kvverti.enim.Util;
import kvverti.enim.Keys;

public class AnimationParser {

	/** The AbieScript file that is being parsed */
	private IResource file;
	private ResourceLocation source;

	public AnimationParser() { }

	public AbieScript parse(IResource file) {

		this.file = file;
		this.source = file.getResourceLocation();
		try { return parseFrames(parseTokens(parseSource())); }
		catch(IndexOutOfBoundsException e) { throw new AbieParseException(source + ": Reached end of file while parsing", e); }
		finally { this.file = null; this.source = null; }
	}

	/**
	 * Turns the source of the file into typed tokens. No validation is done on the tokens.
	 * Throws if source cannot be converted to a token.
	 */
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

			throw new AbieIOException(source + ": Could not read input stream", e);
		}
	}

	//so we don't create two instances for every statement
	private static final Token[] TOKEN_ARR = new Token[0];

	/** Turns the tokens into statements (commands). Throws if tokens are in invalid statement syntax. */
	private List<Statement> parseTokens(List<Token> tokens) {

		List<Statement> statements = new ArrayList<>();
		for(int i = 0; i < tokens.size(); ) {

			if(tokens.get(i).getTokenType() == TokenType.COMMAND) {

				String cmdName = tokens.get(i).getValue();
				Token[] arr = tokens.subList(++i, i += tillNextCmd(tokens, i)).toArray(TOKEN_ARR);
				statements.add(Statement.compile(cmdName, arr));

			} else throw new AbieSyntaxException(source + ": Expected command at token " + i + ": " + tokens.get(i));
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

	/**
	 * Turns statements into an AbieScript animation with defines and frames. AbieScript statements are in this order:
	 *     - defines
	 *     - freq (optional)
	 *     - frames
	 * Out of order statements will cause an error.
	 */
	private AbieScript parseFrames(List<Statement> statements) {

		//does not do bounds checking
		Set<String> defines = getDefines(statements);
		int freq = getFreq(statements);
		assert freq > 0 : freq;
        AnimationFrame init = getInitFrame(statements);
		List<AnimationFrame> frames = getFrames(statements);
		assert statements.isEmpty() : statements;
		return compile(init, frames, defines, freq);
	}

	/** Returns the defines from the given list. The defines must be contiguous. */
	private Set<String> getDefines(List<Statement> statements) {

		Set<String> res = new HashSet<>();
		for(Iterator<Statement> itr = statements.iterator(); itr.hasNext(); ) {

			Statement s = itr.next();
			if(s.getStatementType() == StatementType.DEFINITION) {

				res.add(((StateDefine) s).getDefine());
				itr.remove();
			} else break;
		}
		return res;
	}

	/** Returns the freq specified in the first statement in the list, or 1 if there is none. */
	private int getFreq(List<Statement> statements) {

		Statement s = statements.get(0);
		if(s.getStatementType() == StatementType.FREQUENCY) {

			statements.remove(0);
			return ((StateFreq) s).getFreq();
		}
		return 1;
	}

	/**
	 * Returns the frames specified by the statements. The statements must be composed of anemes, modifiers, pause statements,
	 * and start and end markers. Frames may be simple (consisting of a single aneme with optional modifier) or compound
	 * (consisting of zero or more anemes enclosed in braces with optional modifier before the opening brace).
	 */
	private List<AnimationFrame> getFrames(List<Statement> statements) {

		List<AnimationFrame> res = new ArrayList<>();
		if(statements.isEmpty())
			throw new AbieSyntaxException(source + ": No frames specified");
		for(Iterator<Statement> itr = statements.iterator(); itr.hasNext(); ) {

			Statement s = itr.next();
			itr.remove();
			switch(s.getStatementType()) {

				case PAUSE:
					res.add(getPause((StatePause) s));
					break;
				case REPEAT:
				case OVER:
					res.add(getModifierFrame((StateFrameModifier) s, itr));
					break;
				case START_FRAME:
					res.add(getCompoundFrame(itr));
					break;
				default:
					if(!s.isAneme())
						throw new AbieSyntaxException(source + ": Statement not valid for frames portion of script: " + s);
					res.add(getSimpleFrame((StateAneme) s));
			}
		}
		return res;
	}

	/** Compiles the init frame, if any, else returns an empty frame. */
	private AnimationFrame getInitFrame(List<Statement> statements) {

		Iterator<Statement> itr = statements.iterator();
		Statement s = itr.next();
		if(s.getStatementType() == StatementType.INIT) {

			itr.remove();
			return getModifierFrame((StateFrameModifier) s, itr);
		}
		return AnimationFrame.compile(null, ANEME_ARR);
	}

	/** Compiles a pause frame from the current statement. */
	private AnimationFrame getPause(StatePause current) {

		return AnimationFrame.compilePause(current.getPauseDuration());
	}

	/** Compiles a frame (simple or compound) with a modifier using statements from the iterator. */
	private AnimationFrame getModifierFrame(StateFrameModifier current, Iterator<Statement> itr) {

		Statement s = itr.next();
		itr.remove();
		if(s.getStatementType() != StatementType.START_FRAME && !s.isAneme())
			throw new AbieSyntaxException(source + ": Statement is neither an aneme nor the start of a compound frame: " + s);
		StateAneme[] anemes = s.getStatementType() == StatementType.START_FRAME ?
			getMultiAnemes(itr)
			: new StateAneme[] { (StateAneme) s };
		return AnimationFrame.compile(current, anemes);
	}

	/** Compiles a compound frame using statements from the iterator. The start of frame token is already consumed. */
	private AnimationFrame getCompoundFrame(Iterator<Statement> itr) {

		return AnimationFrame.compile(null, getMultiAnemes(itr));
	}

	//so we don't have to create so many arrays.
	private static final StateAneme[] ANEME_ARR = new StateAneme[0];

	/** Gets anemes until an end of frame statement is encountered. */
	private StateAneme[] getMultiAnemes(Iterator<Statement> itr) {

		List<StateAneme> temp = new ArrayList<>(48); //shouldn't be too many in one frame, right?
		Statement s = itr.next();
		itr.remove();
		while(s.getStatementType() != StatementType.END_FRAME) {

			if(!s.isAneme())
				throw new AbieSyntaxException(source + ": Statement is not an aneme but is in a compound frame: " + s);
			temp.add((StateAneme) s);
			s = itr.next();
			itr.remove();
		}
		return temp.toArray(ANEME_ARR);
	}

	/** Compiles a simple frame from the given aneme. */
	private AnimationFrame getSimpleFrame(StateAneme aneme) {

		return AnimationFrame.compile(null, new StateAneme[] { aneme });
	}

	private AbieScript compile(AnimationFrame init, List<AnimationFrame> frames, Set<String> defines, int freq) {

		List<AbieScript.Frame> frameList = new ArrayList<>();
		Map<String, Vec3f[]> frameMap = new HashMap<>();
		for(String define : defines)
			frameMap.put(define, new Vec3f[] { Vec3f.ORIGIN, Vec3f.ORIGIN });
		fillInit(init, frameMap);
		for(AnimationFrame frame : frames) {

			Util.validate(frame.anemes(),
				aneme -> defines.contains(aneme.getSpecifiedElement()),
				aneme -> new AbieSyntaxException(source + ": Undefined element: " + aneme.getSpecifiedElement()));
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

			switch(modifier.getStatementType()) {

				case REPEAT:
					fillRepeat(frames, animFrame, trueFrame, modifier.getIntModifier(), freq);
					break;
				case OVER:
					fillOver(frames, animFrame, trueFrame, modifier.getIntModifier(), freq);
					break;
				default:
					throw new AssertionError(modifier.getStatementType());
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

				Vec3f[] original = prevCopy.get(aneme.getSpecifiedElement());
				Vec3f[] angles = prevFrame.get(aneme.getSpecifiedElement()).clone();
				Vec3f[] trans = aneme.getTransforms();
				float ft = (float) aneme.getTransformationFunction().applyAsDouble((float) n / duration);
				float x, y, z;
				x = trans[0].x == 0.0f ? angles[0].x : original[0].x + trans[0].x * ft;
				y = trans[0].y == 0.0f ? angles[0].y : original[0].y + trans[0].y * ft;
				z = trans[0].z == 0.0f ? angles[0].z : original[0].z + trans[0].z * ft;
				angles[0] = Vec3f.of(x, y, z);
				x = trans[1].x == 0.0f ? angles[1].x : original[1].x + trans[1].x * ft;
				y = trans[1].y == 0.0f ? angles[1].y : original[1].y + trans[1].y * ft;
				z = trans[1].z == 0.0f ? angles[1].z : original[1].z + trans[1].z * ft;
				angles[1] = Vec3f.of(x, y, z);
				prevFrame.put(aneme.getSpecifiedElement(), angles);
			}
			frames.add(new AbieScript.Frame(new HashMap<>(prevFrame)));
		}
	}

	private static void fillInit(AnimationFrame frame, Map<String, Vec3f[]> frameMap) {

		for(StateAneme aneme : frame.anemes()) {

			Vec3f[] angles = frameMap.get(aneme.getSpecifiedElement()).clone();
			Vec3f[] trans = aneme.getTransforms();
			float ft = (float) aneme.getTransformationFunction().applyAsDouble(1.0);
			float x, y, z;
			x = trans[0].x == 0.0f ? angles[0].x : trans[0].x * ft;
			y = trans[0].y == 0.0f ? angles[0].y : trans[0].y * ft;
			z = trans[0].z == 0.0f ? angles[0].z : trans[0].z * ft;
			angles[0] = Vec3f.of(x, y, z);
			x = trans[1].x == 0.0f ? angles[1].x : trans[1].x * ft;
			y = trans[1].y == 0.0f ? angles[1].y : trans[1].y * ft;
			z = trans[1].z == 0.0f ? angles[1].z : trans[1].z * ft;
			angles[1] = Vec3f.of(x, y, z);
			frameMap.put(aneme.getSpecifiedElement(), angles);
		}
	}
}