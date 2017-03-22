package kvverti.enim.entity.animation;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

import net.minecraft.entity.Entity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import kvverti.enim.entity.EntityInfo;

import static com.google.common.base.Preconditions.checkNotNull;
import static kvverti.enim.Enim.ANIM_TYPE_REGISTRY;

/** The conditions under which an animation may play */
public final class AnimType extends IForgeRegistryEntry.Impl<AnimType> implements IStringSerializable {

	/** Whether this animation type defines a looped animation. */
	private final boolean looped;

	/**
	 * The default animation predicate for this type. If looped, this determines if the animation should play. If not
	 * looped, this determines if the animation should restart.
	 */
	private final AnimPredicate<?> defaultPred;

	/**
	 * A map containing custom predicates for entities. This is really a map of {@code Class<T extends Entity>, Predicate<T>}
	 * for some type T.
	 */
	private final Map<Class<? extends Entity>, AnimPredicate<?>> customPreds = new HashMap<>();

	public AnimType(boolean loop, AnimPredicate<Entity> defaultPredicate) {

		looped = loop;
		defaultPred = checkNotNull(defaultPredicate);
	}

	@Override
	public String getName() { return getRegistryName().toString(); }

	public boolean isLooped() { return looped; }

	/**
	 * Returns whether the given entity should animate under this type. The entity will be matched with the most specific
	 * predicate registered in this type, the result of which is queried and returned.
	 * @param entity The entity to possibly animate
	 * @param info Other entity information
	 * @return Whether the entity should animate
	 * @throws NullPointerException If entity is null, or if info is null and the predicate used does not accept null.
	 */
	public boolean shouldAnimate(Entity entity, EntityInfo info) {

		return getAnimPredicate(entity).shouldAnimate(entity, info);
	}

	@SuppressWarnings("unchecked")
	private <T extends Entity> AnimPredicate<T> getAnimPredicate(T entity) {

		Class<? extends Entity> cls = entity.getClass();
		while(Entity.class.isAssignableFrom(cls))
			if(customPreds.containsKey(cls))
				return (AnimPredicate<T>) customPreds.get(cls);
			else
				cls = (Class<? extends Entity>) cls.getSuperclass();
		return (AnimPredicate<T>) defaultPred;
	}

	/**
	 * Registers a custom animation predicate for the given animation and entity types. This allows one to customize
	 * animation conditions to a particular entity type. For example, one could use fields specific to one entity class
	 * to determine whether entities of that class are animated.
	 * @param <T> The type of entity
	 * @param cls The entity type
	 * @param predicate The predicate
	 * @throws NullPointerException If any of the parameters are null
	 */
	public <T extends Entity> void setCustomAnimPredicate(Class<T> cls, AnimPredicate<? super T> predicate) {

		customPreds.put(checkNotNull(cls), checkNotNull(predicate));
	}

	/** Json adapter for Animation.Type */
	public static class Adapter extends TypeAdapter<AnimType> {

		@Override
		public AnimType read(JsonReader in) throws IOException {

			String name = in.nextString();
			return ANIM_TYPE_REGISTRY.getValue(new ResourceLocation(name));
		}

		@Override
		public void write(JsonWriter out, AnimType value) throws IOException {

			out.value(value.getName());
		}
	}
}