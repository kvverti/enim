package kvverti.enim.modelsystem;

import net.minecraft.util.ResourceLocation;

public final class Texture {

	private final ResourceLocation image;
	private final int xSize;
	private final int ySize;

	public Texture(ResourceLocation loc, int length, int height) {

		image = loc;
		xSize = length;
		ySize = height;
	}

	public int getXSize() {

		return xSize;
	}

	public int getYSize() {

		return ySize;
	}

	public ResourceLocation getTexture() {

		return image;
	}
}