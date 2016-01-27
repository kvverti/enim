package kvverti.enim.entity;

import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

public final class EntityRandomCounters {

	private static final WeakHashMap<Entity, Integer> counters = new WeakHashMap<>();

	public static int get(Entity entity) {

		Integer i = counters.get(entity);
		if(i != null) return i;

		int result = 0;
		result ^= (int) entity.getUniqueID().getMostSignificantBits() & (-1L >>> 32);
		result = result < 0 ? -result : result;
		counters.put(entity, result);
		return result;
	}
}