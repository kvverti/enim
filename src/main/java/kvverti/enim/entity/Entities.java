package kvverti.enim.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import kvverti.enim.Ticker;

/** 
 * Utility class for working with {@link Entity}s and {@link TileEntity}s. Also contains convenience methods for common objects
 * found in the {@link Minecraft} class.
 */
public final class Entities {

	//private static volatile EnimTicker masterTicker;

	/** Construction disallowed */
	private Entities() { }

	public static float toRadians(float degrees) {

		return degrees * (float) Math.PI / 180.0f;
	}

	public static float toDegrees(float radians) {

		return radians * (180.0f / (float) Math.PI);
	}

	public static int randomCounterFor(Entity entity) {

		return Ticker.INSTANCE.ticks(entity);
	}

	public static int randomCounterFor(TileEntity tile) {

		return Ticker.INSTANCE.ticks(tile);
	}

	public static int jumpTime(Entity entity) {

		return Ticker.INSTANCE.jumpTicks(entity);
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
}