package kvverti.enim;

import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;

import kvverti.enim.entity.Entities;

/** 
 * Tracks world ticks and entity timers. This class subscribes to various Forge events to keep track of the actions of entities.
 * In multiplayer, Forge events are not available, so this class then uses various entity properties on the client to make a
 * "best guess" estimate as to what the entity is doing. Visible effects may differ when playing multiplayer.
 */
public final class Ticker {

	/** The singleton instance */
	public static final Ticker INSTANCE = new Ticker();
	private static final WeakHashMap<Entity, AtomicInteger> entityCounters = new WeakHashMap<>();
	private static final WeakHashMap<TileEntity, AtomicInteger> tileCounters = new WeakHashMap<>();
	private static final WeakHashMap<Entity, AtomicInteger> jumpCounters = new WeakHashMap<>();

	//slowing down tick rate to match the time returned by World#getTotalWorldTime()
	private byte slow = 0;

	/** Construction disallowed */
	private Ticker() { }

	/**
	 * Subscribed to {@link WorldTickEvent} events. Increments all entity and tile entity timers at the start of every
	 * world tick. This method is only called by Forge on the inetgrated client.
	 */
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event) {

		if(event.phase == Phase.START && ++slow % 3 == 0) {

			entityCounters.values().forEach(AtomicInteger::incrementAndGet);
			tileCounters.values().forEach(AtomicInteger::incrementAndGet);
			for(Iterator<AtomicInteger> itr = jumpCounters.values().iterator(); itr.hasNext(); )
				if(itr.next().getAndIncrement() < 0)
					itr.remove();
		}
	}

	/**
	 * Subscribed to {@link LivingJumpEvent} events. Creates a jump timer if one does not exist for the given entity and
	 * sets it to zero. This method is only called by Forge on the integrated client.
	 */
	@SubscribeEvent
	public void onLivingJump(LivingJumpEvent event) {

		jumpCounters.computeIfAbsent(event.entityLiving, e -> new AtomicInteger()).set(0);
	}

	public int ticks(Entity entity) {

		return entityCounters.computeIfAbsent(entity, this::computeCounter).get()
			+ (isClientIntegrated() ? 0 : worldTime());
	}

	public int ticks(TileEntity tile) {

		return tileCounters.computeIfAbsent(tile, this::computeCounter).get()
			+ (isClientIntegrated() ? 0 : worldTime());
	}

	/*
	 * If in singleplayer, the jump ticks are incremented automatically from zero, so the counter is returned if positive.
	 * However, if in multiplayer, the jump ticks are stored relative to the world time when the entity last jumped,
	 * so the counter value is subtracted from the current world time to get the diference in ticks. As well, in multiplayer
	 * the counters need to be manually reset :( This is a client-side mod after all!
	 */
	public int jumpTicks(Entity entity) {

		if(!isClientIntegrated()) {

			//check for "jumping" and set to 0 if so (rabbits hop with less "bounce" - less than 0.18)
			if(entity instanceof EntityLivingBase && entity.motionY > 0.42f
			|| entity instanceof EntityRabbit     && entity.motionY > 0.10f)
				jumpCounters.computeIfAbsent(entity, c -> new AtomicInteger()).set(worldTime());
		}
		if(jumpCounters.containsKey(entity)) {

			int res = isClientIntegrated() ? jumpCounters.get(entity).get()
				: worldTime() - jumpCounters.get(entity).get();
			return res >= 0 ? res : -1;
		}
		return -1;
	}

	private AtomicInteger computeCounter(Entity entity) {

		int result = Objects.hash(entity.getUniqueID());
		AtomicInteger counter = new AtomicInteger(result);
		entityCounters.put(entity, counter);
		return counter;
	}

	private AtomicInteger computeCounter(TileEntity tile) {

		int result = Objects.hash(tile.getPos());
		AtomicInteger counter = new AtomicInteger(result);
		tileCounters.put(tile, counter);
		return counter;
	}

	/** Returns the total world time modulated to the size of an {@code int} */
	private int worldTime() {

		return (int) (Entities.theWorld().getTotalWorldTime() % Integer.MAX_VALUE);
	}

	private boolean isClientIntegrated() {

		return Minecraft.getMinecraft().isSingleplayer();
	}
}