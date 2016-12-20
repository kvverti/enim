package kvverti.enim;

import java.lang.reflect.Method;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;

import kvverti.enim.entity.Entities;

/** 
 * Tracks world ticks and entity timers. This class subscribes to various Forge events to keep track of the actions of entities.
 * In multiplayer, Forge events are not available, so this class then uses various entity properties on the client to make a
 * "best guess" estimate as to what the entity is doing. Visible effects may differ when playing multiplayer.
 */
public final class Ticker {

	private static final int MEAN_NUM_ENTITIES = 30;

	/** The singleton instance */
	public static final Ticker INSTANCE = new Ticker();
	private final WeakHashMap<Entity, TickCounter> randomTickCounters = new WeakHashMap<>(MEAN_NUM_ENTITIES);
	private final WeakHashMap<TileEntity, AtomicInteger> tileCounters = new WeakHashMap<>(MEAN_NUM_ENTITIES);
	private final WeakHashMap<Entity, TickCounter> jumpCounters = new WeakHashMap<>(MEAN_NUM_ENTITIES);
	private final WeakHashMap<Entity, AtomicBoolean> attackTargetFlags = new WeakHashMap<>(MEAN_NUM_ENTITIES);

	/** Construction disallowed */
	private Ticker() { }

	/**
	 * Subscribed to {@link LivingJumpEvent} events. Creates a jump timer if one does not exist for the given entity and
	 * sets it to zero. This method is only called by Forge on the integrated client.
	 */
	@SubscribeEvent
	public void onLivingJump(LivingJumpEvent event) {

		jumpCounters.put(event.entityLiving, new TickCounter(event.entityLiving));
	}

	/**
	 * Subscribed to {@link LivingSetAttackTargetEvent} events. Maintains a boolean value for each entity that is true when the
	 * entity has an attack target and false when it does not. This method is only called by Forge on the integrated client.
	 */
	@SubscribeEvent
	public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {

		attackTargetFlags.computeIfAbsent(event.entityLiving, e -> new AtomicBoolean()).set(event.target != null);
	}

	/** Returns the global tick counter for the given entity, optionally scaled for speed */
	public int ticks(Entity entity, boolean scaled) {

		return randomTickCounters.computeIfAbsent(entity, TickCounter::new).tickValue(scaled);
	}

	/** Returns the global tick counter for the given tile entity. */
	public int ticks(TileEntity tile) {

		return tileCounters.computeIfAbsent(tile, this::computeCounter).get() + worldTime();
	}

	/**
	 * If in singleplayer, the jump ticks are incremented automatically from zero, so the counter is returned if positive.
	 * However, if in multiplayer, the jump ticks are stored relative to the world time when the entity last jumped,
	 * so the counter value is subtracted from the current world time to get the diference in ticks. As well, in multiplayer
	 * the counters need to be manually reset :( This is a client-side mod after all!
	 */
	public int jumpTicks(Entity entity, boolean scaled) {

		if(!isClientIntegrated()) {

			//check for "jumping" and set to 0 if so (rabbits hop with less "bounce" - less than 0.18)
			if(entity instanceof EntityLivingBase && entity.motionY > 0.42f
			|| entity instanceof EntityRabbit     && entity.motionY > 0.10f)
				jumpCounters.put(entity, new TickCounter(entity));
		}
		//because Forge doesn't send jump events for the EntitySlime class
		if(entity.getClass() == EntitySlime.class && entity.motionY > 0.20f)
			jumpCounters.put(entity, new TickCounter(entity));
		TickCounter c = jumpCounters.get(entity);
		if(c != null) {

			int res = c.offsetTickValue(scaled);
			return res >= 0 ? res : -1;
		}
		return -1;
	}

	/**
	 * Returns whether the given entity has an attack target. There does not seem to be a way to determine this on a dedicated
	 * server environment, so it will return false in all cases in multiplayer.
	 */
	public boolean hasAttackTarget(Entity entity) {

		AtomicBoolean b = attackTargetFlags.get(entity);
		return b != null ? b.get() : false;
	}

	private AtomicInteger computeCounter(TileEntity tile) {

		int result = Objects.hash(tile.getPos());
		AtomicInteger counter = new AtomicInteger(result);
		tileCounters.put(tile, counter);
		return counter;
	}

	/** Returns the total world time modulated to the size of an {@code int} */
	private static int worldTime() {

		WorldClient world = Entities.theWorld();
		return world == null ? 0 : (int) world.getTotalWorldTime();
	}

	private boolean isClientIntegrated() {

		return Minecraft.getMinecraft().isSingleplayer();
	}

	/**
	 * Holds two counter values for an entity - one based on the world time and one based on the entity's previous movement speed.
	 * This class does not hold a strong reference to the passed entity.
	 */
	private static class TickCounter {

		/** Offset based on the entity so that different entities have different times. */
		private final int tickOffsetSeed;

		/** The initial world time at the time of creation */
		private final int initWorldTime;

		/** The current speed-accumulative tick value */
		private int speedAccTick;

		/** A weak reference to the entity for which this counter was created. */
		private WeakReference<Entity> entityRef;

		public TickCounter(Entity entity) {

			tickOffsetSeed = Objects.hash(entity.getUniqueID());
			initWorldTime = worldTime();
			prevWorldTime = initWorldTime;
			speedAccTick = 0;
			entityRef = new WeakReference<>(entity);
		}

		/** Returns the tick value for the entity, optionally scaled with movement */
		public int tickValue(boolean scaled) {

			Entity e = entityRef.get();
			if(e == null) //why do we still exist?
				return 0;
			return tickOffsetSeed + (scaled ? updateSpeedAcc(e) : worldTime());
		}

		/** Returns the tick value for the entity, optionally scaled with movement, relative to the initial time at creation. */
		public int offsetTickValue(boolean scaled) {

			Entity e = entityRef.get();
			if(e == null) //why do we still exist?
				return 0;
			return scaled ? updateSpeedAcc(e) : worldTime() - initWorldTime;
		}

		private int prevWorldTime;

		/** Updates and accumulates the speed counter. */
		private int updateSpeedAcc(Entity e) {

			int worldTime = worldTime();
			if(worldTime - prevWorldTime > 0) {

				prevWorldTime = worldTime;
				float speed = Entities.speedSq(e);
				if(speed <= 0.0025f)
					speed = 0.0f;
				float scalar = Entities.interpolate(1.0f, 4.0f, speed * 100); //magic :o
				return speedAccTick += (int) scalar;
			}
			return speedAccTick;
		}
	}
}