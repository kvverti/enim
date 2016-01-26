package kvverti.enim.modelsystem;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class Statement {

	private final Token[] args;
	private final StatementType type;

	private Statement(StatementType t, Token... ts) {

		args = ts;
		type = t;
	}

	/* Guaranteed to be syntactically correct */
	public Token[] getTokens() {

		return args;
	}

	public StatementType getStatementType() {

		return type;
	}

	public int tokenCount() {

		return args.length;
	}

	public static Statement compile(StatementType type, Token... args) throws SyntaxException {

		validate(type, args);
		return new Statement(type, args);
	}

	private static void validate(StatementType type, Token[] args) throws SyntaxException {

		int flag = -1;
		TokenType[] types = type.getTokenTypes();
		for(int i = 0; i < types.length; i++) {

			if(types[i] != args[i].getTokenType()) flag = i;
		}
		if(flag >= 0)
			throw new SyntaxException("Unexpected argument type for statement " + type + ": "
				+ "expected " + types[flag] + ", got " + args[flag]);
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder().append(type.getName());
		for(Token t : args) {

			sb.append(' ').append(t.getValue());
		}
		return sb.toString();
	}
}