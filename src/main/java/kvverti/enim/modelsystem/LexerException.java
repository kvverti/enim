package kvverti.enim.modelsystem;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LexerException extends ParserException {

	public LexerException() { }

	public LexerException(String message) {

		super(message);
	}

	public LexerException(Throwable cause) {

		super(cause);
	}

	public LexerException(String message, Throwable cause) {

		super(message, cause);
	}
}