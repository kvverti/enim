package kvverti.enim;

import java.lang.reflect.Method;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
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
 * Tracks world ticks and entity timers. Most functionality formerly provided by this class has been replaced with the
 * {@link kvverti.enim.entity.animation} package. This class will be removed when tile entities move to the new system.
 */
public final class Ticker {

	private static final int MEAN_NUM_ENTITIES = 30;

	/** The singleton instance */
	public static final Ticker INSTANCE = new Ticker();
	private final WeakHashMap<TileEntity, AtomicInteger> tileCounters = new WeakHashMap<>(MEAN_NUM_ENTITIES);

	/** Construction disallowed */
	private Ticker() { }

	/** Returns the global tick counter for the given tile entity. */
	public int ticks(TileEntity tile) {

		return tileCounters.computeIfAbsent(tile, this::computeCounter).get() + worldTime();
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
}