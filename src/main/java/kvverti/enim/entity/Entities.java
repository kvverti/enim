package kvverti.enim.entity;

import java.util.WeakHashMap;
import java.util.Objects;
//import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;

/** Utility class for working with Entitys and TileEntitys. */
public final class Entities {

	//private static volatile EnimTicker masterTicker;

	private Entities() { }

	public static float toRadians(float degrees) {

		return degrees * (float) Math.PI / 180.0f;
	}

	public static float toDegrees(float radians) {

		return radians * (180.0f / (float) Math.PI);
	}

	public static int randomCounterFor(Entity entity) {

		//return TickEventHandler.INSTANCE.ticks(entity);
		return Objects.hash(entity.getUniqueID()) + (int) (theWorld().getTotalWorldTime() % Integer.MAX_VALUE);
	}

	public static int randomCounterFor(TileEntity tile) {

		//return TickEventHandler.INSTANCE.ticks(tile);
		return Objects.hash(tile.getPos()) + (int) (theWorld().getTotalWorldTime() % Integer.MAX_VALUE);
	}

	@SuppressWarnings("deprecation")
	public static int jumpTime(Entity entity) {

		return TickEventHandler.INSTANCE.jumpTicks(entity);
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

	public static WorldClient theWorld() {

		return Minecraft.getMinecraft().theWorld;
	}

	/*public static synchronized void initTicker() {

		if(masterTicker == null) {

			masterTicker = new EnimTicker();
			new Thread(masterTicker, "Enim Ticker").start();
		}
	}

	public static int currentTick() {

		return masterTicker.currentTick();
	}

	private static final class EnimTicker implements Runnable {

		private final AtomicInteger counter = new AtomicInteger();
		private final Minecraft mc = Minecraft.getMinecraft();

		/* count game ticks *//*
		@Override
		public void run() {

			while(true) {

				if(!mc.isGamePaused())
					kvverti.enim.Logger.info(counter.incrementAndGet());
				try { Thread.sleep(50); }
				catch(InterruptedException e) { }
			}
		}

		public int currentTick() {

			return counter.get();
		}
	}*/

	@Deprecated
	public static class TickEventHandler {

		public static final TickEventHandler INSTANCE = new TickEventHandler();
		private static final WeakHashMap<Entity, IntCounter> entityCounters = new WeakHashMap<>();
		private static final WeakHashMap<TileEntity, IntCounter> tileCounters = new WeakHashMap<>();
		private static final WeakHashMap<Entity, IntCounter> jumpCounters = new WeakHashMap<>();

		private byte slow = 0;

		private TickEventHandler() { }

		@SubscribeEvent
		public void onWorldTick(WorldTickEvent event) {

			if(event.phase == Phase.START && ++slow % 3 == 0) {

				entityCounters.values().forEach(IntCounter::preIncrement);
				tileCounters.values().forEach(IntCounter::preIncrement);
				jumpCounters.values().stream()
					.filter(counter -> counter.get() >= 0)
					.forEach(IntCounter::preIncrement);
			}
		}

		@SubscribeEvent
		public void onLivingJump(LivingJumpEvent event) {

			kvverti.enim.Logger.info("Jumped: " + event.entityLiving.motionY);
			jumpCounters.computeIfAbsent(event.entityLiving, e -> new IntCounter()).reset();
		}

		public int ticks(Entity entity) {

			return entityCounters.computeIfAbsent(entity, this::computeCounter).get();
		}

		public int ticks(TileEntity tile) {

			return tileCounters.computeIfAbsent(tile, this::computeCounter).get();
		}

		public int jumpTicks(Entity entity) {

			IntCounter c = jumpCounters.get(entity);
			return c != null ? c.get() : -1;
		}

		private IntCounter computeCounter(Entity entity) {

			int result = Objects.hash(entity.getUniqueID());
			IntCounter counter = new IntCounter(result);
			entityCounters.put(entity, counter);
			return counter;
		}

		private IntCounter computeCounter(TileEntity tile) {

			int result = Objects.hash(tile.getPos());
			IntCounter counter = new IntCounter(result);
			tileCounters.put(tile, counter);
			return counter;
		}
	}

	@Deprecated
	private static class IntCounter {

		private int value;

		public IntCounter() {

			this(0);
		}

		public IntCounter(int initial) {

			value = initial;
		}

		public int preIncrement() {

			return ++value;
		}

		public int postIncrement() {

			return value++;
		}

		public int get() {

			return value;
		}

		public void reset() {

			value = 0;
		}
	}
}