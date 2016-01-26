package kvverti.enim.modelsystem;

import java.util.List;
import java.util.ArrayList;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class Parser {

	private final List<Token> tokens;

	public Parser(List<Token> tokens) {

		this.tokens = tokens;
	}

	public List<Statement> parse() throws ParserException {

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