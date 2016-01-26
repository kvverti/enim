package kvverti.enim.modelsystem;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParserException extends ENIMException {

	public ParserException() { }

	public ParserException(String message) {

		super(message);
	}

	public ParserException(Throwable cause) {

		super(cause);
	}

	public ParserException(String message, Throwable cause) {

		super(message, cause);
	}
}