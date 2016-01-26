package kvverti.enim.modelsystem;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TokenSyntaxException extends SyntaxException {

	private final String token;

	public TokenSyntaxException(String message, String token) {

		super(message);
		this.token = token;
	}

	public String getToken() {

		return token;
	}
}