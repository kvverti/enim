package kvverti.enim.entity.animation;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.multiplayer.WorldClient;

import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheBuilder;

import kvverti.enim.Util;
import kvverti.enim.entity.Entities;
import kvverti.enim.entity.GEntity;

public final class EntityFrameTimers {

	/**
	 * Holds all counters indexed by animation type and entity instance. Looping animations all share one set of counters,
	 * while nonlooping animation types have separate counters.
	 */
	private static final LoadingCache<TimerKey, TickCounter> counters = CacheBuilder.newBuilder()
		.expireAfterAccess(5, TimeUnit.SECONDS)
		.initialCapacity(100)
		.maximumSize(500)
		.removalListener(note -> kvverti.enim.Logger.info("Removed counter for entity: %s", note.getKey()))
		.build(new CacheLoader<TimerKey, TickCounter>() {

			@Override
			public TickCounter load(TimerKey key) {

				kvverti.enim.Logger.info("Created tick counter for entity: %s", key);
				return new TickCounter(key.entity);
			}
		});

	/** Restarts the counter for the given type and entity */
	public static void restart(AnimType type, GEntity entity) {

		counters.getUnchecked(new TimerKey(type, entity)).resetTime();
	}

	public static int timeValue(AnimType type, GEntity entity, boolean scaled) {

		TimerKey key = new TimerKey(type, entity);
		if(!type.isLooped()) {

			TickCounter c = counters.getIfPresent(key);
			return c != null ? c.offsetTickValue(scaled) : -1;
		} else return counters.getUnchecked(key).tickValue(scaled);
	}

	public static void clearAll() {

		counters.invalidateAll();
	}

	/** The key type for the counter cache */
	private static final class TimerKey {

		/** The animation type of this counter */
		public final AnimType animType;

		/** A reference to the entity */
		public final GEntity entity;

		public TimerKey(AnimType a, GEntity e) {

			animType = a;
			entity = e;
		}

		/** Two keys are equal if their entities are equal and the animation types are either both looped or identical */
		@Override
		public boolean equals(Object o) {

			if(!(o instanceof TimerKey))
				return false;
			TimerKey k = (TimerKey) o;
			return entity.equals(k.entity) && (animType.isLooped() ? k.animType.isLooped() : animType == k.animType);
		}

		@Override
		public int hashCode() {

			return entity.hashCode() + (animType.isLooped() ? 1 : animType.hashCode());
		}

		@Override
		public String toString() {

			return String.format("type=%s, entityClass=%s",
				animType.isLooped() ? "<looped>" : animType.getName(), entity.getEntityClass().getSimpleName());
		}
	}

	/**
	 * Holds two counter values for an entity - one based on the world time and one based on the entity's previous movement speed.
	 */
	private static class TickCounter {

		/** Offset based on the entity so that different entities have different times. */
		private final int tickOffsetSeed;

		/** The initial world time */
		private int initWorldTime;

		/** The current speed-accumulative tick value */
		private int speedAccTick;

		/** A reference to the entity for which this counter was created. */
		private final GEntity entity;

		public TickCounter(GEntity e) {

			tickOffsetSeed = e.getCounterSeed();
			initWorldTime = worldTime();
			prevWorldTime = initWorldTime;
			speedAccTick = 0;
			entity = e;
		}

		/** Returns the tick value for the entity, optionally scaled with movement */
		public int tickValue(boolean scaled) {

			return tickOffsetSeed + (scaled ? updateSpeedAcc(entity) : worldTime());
		}

		/** Returns the tick value for the entity, optionally scaled with movement, relative to the initial time. */
		public int offsetTickValue(boolean scaled) {

			return scaled ? updateSpeedAcc(entity) : worldTime() - initWorldTime;
		}

		public void resetTime() {

			initWorldTime = worldTime();
			speedAccTick = 0;
		}

		private int prevWorldTime;

		/** Updates and accumulates the speed counter. */
		private int updateSpeedAcc(GEntity e) {

			int worldTime = worldTime();
			if(worldTime - prevWorldTime > 0) {

				prevWorldTime = worldTime;
				float speed = !e.isTileEntity() ? Entities.speedSq(e.getEntity()) : 0.0f;
				if(speed <= 0.0025f)
					speed = 0.0f;
				float scalar = Entities.interpolate(1.0f, 4.0f, speed * 100); //magic :o
				return speedAccTick += (int) scalar;
			}
			return speedAccTick;
		}

		/** Returns the total world time modulated to the size of an {@code int} */
		private static int worldTime() {

			WorldClient world = Entities.theWorld();
			return world == null ? 0 : (int) world.getTotalWorldTime();
		}
	}
}