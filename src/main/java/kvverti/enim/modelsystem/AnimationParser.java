package kvverti.enim.modelsystem;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

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

				} catch(SyntaxException|IndexOutOfBoundsException e) {

					throw new ParserException(e);
				}

			} else throw new ParserException("Expected command at token " + i + ": " + tokens.get(i));
		}
		return statements;
	}
}