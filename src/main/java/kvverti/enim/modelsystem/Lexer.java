package kvverti.enim.modelsystem;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

import net.minecraft.client.resources.IResource;

public final class Lexer {

	private final IResource file;

	public Lexer(IResource loc) {

		file = loc;
	}

	public List<Token> lex() throws LexerException {

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
}