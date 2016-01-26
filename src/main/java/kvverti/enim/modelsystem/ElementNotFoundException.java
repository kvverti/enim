package kvverti.enim.modelsystem;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ElementNotFoundException extends ParserException {

	public ElementNotFoundException() { }

	public ElementNotFoundException(String message) {

		super(message);
	}
}