package kvverti.enim.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

/** 
 * Utility class for working with {@link Entity}s and {@link TileEntity}s. Also contains convenience methods for common objects
 * found in the {@link Minecraft} class.
 */
public final class Entities {

	/** Construction disallowed */
	private Entities() { }

	public static float toRadians(float degrees) {

		return degrees * (float) Math.PI / 180.0f;
	}

	public static float toDegrees(float radians) {

		return radians * (180.0f / (float) Math.PI);
	}

	public static float speedSq(Entity entity) {

		double x = entity.posX - entity.lastTickPosX;
		double z = entity.posZ - entity.lastTickPosZ;
		return (float) (x * x + z * z);
	}

	public static float interpolate(float start, float end, float percent) {

		return start * (1.0f - percent) + end * percent;
	}

	public static TextureManager textureManager() {

		return Minecraft.getMinecraft().getTextureManager();
	}

	public static IReloadableResourceManager resourceManager() {

		return (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
	}

	public static EntityPlayerSP thePlayer() {

		return Minecraft.getMinecraft().player;
	}

	public static WorldClient theWorld() {

		return Minecraft.getMinecraft().world;
	}
}