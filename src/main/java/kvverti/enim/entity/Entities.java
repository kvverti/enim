package kvverti.enim.entity;

import java.util.WeakHashMap;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

/** Utility class for working with Entitys and TileEntitys. */
public final class Entities {

	private static final WeakHashMap<Entity, Integer> counters = new WeakHashMap<>();
	private static final WeakHashMap<TileEntity, Integer> tileCounters = new WeakHashMap<>();

	private Entities() { }

	public static float toRadians(float degrees) {

		return degrees * (float) Math.PI / 180.0f;
	}

	public static float toDegrees(float radians) {

		return radians * (180.0f / (float) Math.PI);
	}

	public static int randomCounterFor(Entity entity) {

		return counters.computeIfAbsent(entity, Entities::computeCounter).intValue();
	}

	private static int computeCounter(Entity entity) {

		int result = Objects.hash(entity.getUniqueID());
		result = result < 0 ? -result : result;
		counters.put(entity, result);
		return result;
	}

	public static int randomCounterFor(TileEntity tile) {

		return tileCounters.computeIfAbsent(tile, Entities::computeCounter).intValue();
	}

	private static int computeCounter(TileEntity tile) {

		int result = Objects.hash(tile.getWorld(), tile.getPos());
		result = result < 0 ? -result : result;
		tileCounters.put(tile, result);
		return result;
	}

	public static TextureManager textureManager() {

		return Minecraft.getMinecraft().getTextureManager();
	}

	public static IReloadableResourceManager resourceManager() {

		return (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
	}
}