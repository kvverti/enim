package kvverti.enim.modelsystem;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SyntaxException extends ENIMException {

	public SyntaxException() { }

	public SyntaxException(String message) {

		super(message);
	}

	public SyntaxException(Throwable cause) {

		super(cause);
	}

	public SyntaxException(String message, Throwable cause) {

		super(message, cause);
	}
}