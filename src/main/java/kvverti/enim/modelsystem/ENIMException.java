package kvverti.enim.modelsystem;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ENIMException extends Exception {

	public ENIMException() { }

	public ENIMException(String message) {

		super(message);
	}

	public ENIMException(Throwable cause) {

		super(cause);
	}

	public ENIMException(String message, Throwable cause) {

		super(message, cause);
	}
}