package kvverti.enim.entity;

import java.util.WeakHashMap;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

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

		return counters.computeIfAbsent(entity, Entities::computeCounter).intValue() + entity.ticksExisted;
	}

	private static int computeCounter(Entity entity) {

		int result = Objects.hash(entity.getUniqueID());
		result = result < 0 ? -result : result;
		counters.put(entity, result);
		return result;
	}

	public static int randomCounterFor(TileEntity tile) {

		return tileCounters.computeIfAbsent(tile, Entities::computeCounter).intValue()
			+ WorldTickEventHandler.INSTANCE.count();
	}

	private static int computeCounter(TileEntity tile) {

		int result = Objects.hash(tile.getPos());
		result = result < 0 ? -result : result;
		tileCounters.put(tile, result);
		return result;
	}

	public static float interpolate(float start, float end, float percent) {

		return start + (end - start) * percent;
	}

	public static TextureManager textureManager() {

		return Minecraft.getMinecraft().getTextureManager();
	}

	public static IReloadableResourceManager resourceManager() {

		return (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
	}

	public static EntityPlayerSP thePlayer() {

		return Minecraft.getMinecraft().thePlayer;
	}

	public static class WorldTickEventHandler {

		public static final WorldTickEventHandler INSTANCE = new WorldTickEventHandler();

		private int count = 0;

		private WorldTickEventHandler() { }

		@SubscribeEvent
		public void onWorldTick(WorldTickEvent event) {

			if(event.phase == Phase.START)
				count++;
		}

		public int count() {

			return count / 3;
		}
	}
}