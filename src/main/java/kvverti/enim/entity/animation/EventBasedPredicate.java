package kvverti.enim.entity.animation;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.HashSet;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.minecraft.entity.Entity;

import com.google.common.reflect.TypeToken;

import kvverti.enim.entity.EntityInfo;

import static kvverti.enim.Util.findMethod;
import static kvverti.enim.Util.invokeUnchecked;

/**
 * A class for creating AnimPredicates that leverage the Forge event system. Instead of using the client entity context to
 * determine whether to animate an entity, AnimPredicates created using this class subscribe to a class of EntityEvent and use
 * these events instead. To use this class, subclass it (usually anonymously) and override the implementations of
 * {@link #shouldAnimate(EntityEvent)} and {@link #multiplayerFallback(Entity,EntityInfo)} as needed. Finally, call the
 * {@link #create()} method to obtain an instance of AnimPredicate that implements the functionality described by this class.
 * This instance is automatically registered to the Forge event bus, you do not need to explicitly register anything.
 * <br>
 * A typical use of this class is as follows.
 * <pre>{@code
 *   AnimPredicate<MyEntity> animPred = new EventBasedPredicate<MyEntity, MyEvent>() {
 *       //override methods as needed
 *   }.create();
 * }</pre>
 * Note that this class does <em>not</em> implement the AnimPredicate interface, and so cannot be passed as an argument to the
 * AnimType constructor.
 * <p>
 * <strong>Important:</strong> The proper operation of this class depends on the event type parameter being a valid type, rather than
 * a type variable. This means the following code is not ok.
 * <pre>{@code
 *   public <E extends EntityEvent> EventBasedPredicate<Entity, E> wrong() {
 *       //wrong: E is erased at runtime
 *       return new EventBasedPredicate<Entity, E>(){};
 *   }
 * }</pre>
 * However, the above code is also useless as the methods of this class are meant to be overridden, which cannot be done with a
 * method call.
 */
public abstract class EventBasedPredicate<T extends Entity, E extends EntityEvent> {

	//Minecraft Forge's event bus regsitration implementation
	private static final Method registerImpl = findMethod(
		EventBus.class,
		void.class,
		new String[] { "register" },
		Class.class,
		Object.class,
		Method.class,
		ModContainer.class);

	//the method to register as the event-subscribing method
	private static final Method eventMethod = findMethod(
		EventBasedPredicate.class,
		void.class,
		new String[] { "onEventWrap" },
		EntityEvent.class);

	private final Class<?> eventType = new TypeToken<E>(getClass()){}.getRawType();
	private final Set<Entity> entitiesToAnimate = new HashSet<>(20);

	/** Sole constructor, for use by (usually anonymous) subclasses */
	protected EventBasedPredicate() { }

	/**
	 * Determines from the passed event whether the entity specified in the event should be animated.
	 * The default implementation returns true.
	 */
	protected boolean shouldAnimate(E event) { return true; }

	/** Fallback for when event handling is not available. The default implementation returns false. */
	protected boolean multiplayerFallback(T entity, EntityInfo info) { return false; }

	/* Not private so Forge is happy */
	@SubscribeEvent
	final void onEventWrap(E event) {

		if(shouldAnimate(event))
			entitiesToAnimate.add(event.getEntity());
	}

	/** Returns an instance of AnimPredicate which implements this functionality. You probably should only call this once. */
	public final AnimPredicate<T> create() {

		//register to the Forge event bus
		invokeUnchecked(MinecraftForge.EVENT_BUS, registerImpl, eventType, this, eventMethod, Loader.instance().activeModContainer());
		return new Impl();
	}

	private class Impl extends AnimPredicate.ServerDependentPredicate<T> {

		@Override
		protected boolean computeMultiplayer(T entity, EntityInfo info) {

			return multiplayerFallback(entity, info);
		}

		@Override
		protected boolean computeSingleplayer(T entity, EntityInfo info) {

			return entitiesToAnimate.remove(entity);
		}
	}
}