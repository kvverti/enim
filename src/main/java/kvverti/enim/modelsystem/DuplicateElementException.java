package kvverti.enim.modelsystem;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DuplicateElementException extends ParserException {

	public DuplicateElementException() { }

	public DuplicateElementException(String message) {

		super(message);
	}
}