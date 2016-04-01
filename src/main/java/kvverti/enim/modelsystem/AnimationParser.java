package kvverti.enim.modelsystem;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import net.minecraft.client.resources.IResource;

public final class AnimationParser {

	private final IResource file;

	public AnimationParser(IResource loc) {

		file = loc;
	}

	public List<Token> parseSource() throws LexerException {

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

				} else {

					s.append((char) charValue);
				}
			}
			return tokens;

		} catch(IOException|TokenSyntaxException e) {

			throw new LexerException(e);
		}
	}

	public List<Statement> parseTokens(List<Token> tokens) throws ParserException {

		List<Statement> statements = new ArrayList<>();
		StatementType current;
		for(int i = 0; i < tokens.size(); ) {

			current = StatementType.byName(tokens.get(i).getValue());
			if(current != null) {

				try {
					Token[] arr = tokens.subList(++i, i += current.tokenCount())
						.toArray(new Token[current.tokenCount()]);
					statements.add(Statement.compile(current, arr));

				} catch(SyntaxException e) {

					throw new ParserException(e);

				} catch(IndexOutOfBoundsException e) {

					throw new ParserException("Reached end of file while parsing");
				}

			} else throw new ParserException("Expected command at token " + i + ": " + tokens.get(i));
		}
		return statements;
	}

	private int addPause(List<Statement> statements, int index, List<AnimationFrame> frames) {

		StatePause pause = (StatePause) statements.get(index++);
		frames.add(AnimationFrame.compilePause(pause.getPauseDuration()));
		return index;
	}

	private int addFrame(List<Statement> statements, int index, List<AnimationFrame> frames) throws ParserException {

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

		} catch(SyntaxException e) {

			throw new ParserException(e);

		} catch(IndexOutOfBoundsException e) {

			throw new ParserException("Reached end of file while parsing");
		}
	}

	private int addDefine(List<Statement> statements, int index, Set<String> defines) {

		StateDefine state = (StateDefine) statements.get(index++);
		defines.add(state.getDefine());
		return index;
	}

	public Animation parseFrames(List<Statement> statements) throws ParserException {

		List<AnimationFrame> frames = new ArrayList<>();
		Set<String> defines = new HashSet<>();
		int freq = 1;
		for(int i = 0; i < statements.size(); ) {

			switch(statements.get(i).getStatementType()) {

				case DEFINITION:
					i = addDefine(statements, i, defines);
					break;

				case FREQUENCY:
					if(freq != 1) throw new ParserException("Duplicate freq statement");
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
		try { return Animation.compile(frames, defines, freq); }
		catch(SyntaxException e) { throw new ParserException(e); }
	}
}