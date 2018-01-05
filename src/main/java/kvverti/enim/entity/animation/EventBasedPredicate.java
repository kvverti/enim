package kvverti.enim.entity.animation;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.Collections;
import java.util.WeakHashMap;

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
 * <p>
 * A typical use of this class is as follows.
 * <pre>{@code
 *   AnimPredicate<MyEntity> animPred = new EventBasedPredicate<MyEntity, MyEvent>(...) {
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
 *       return new EventBasedPredicate<Entity, E>(false){};
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
        "register",
        Class.class,
        Object.class,
        Method.class,
        ModContainer.class);

    //the method to register as the event-subscribing method
    private static final Method eventMethod = findMethod(
        EventBasedPredicate.class,
        void.class,
        "onEventWrap",
        EntityEvent.class);

    private final Set<Entity> entitiesToAnimate = Collections.newSetFromMap(new WeakHashMap<>(20));
    private final Class<?> eventType = new TypeToken<E>(getClass()){}.getRawType();
    private final boolean toggle;

    /**
     * Sole constructor, for use by (usually anonymous) subclasses. The parameter determines whether results from
     * {@link #shouldAnimate(EntityEvent)} should be retained until the next result from the same method. Set this to true if your
     * event is a toggle for some state; set to false if your event is a signal of some action. A good rule of thumb is to pass
     * true if the associated AnimType is looped and false if not.
     * @param retainEventResult whether the result of {@link #shouldAnimate(EntityEvent)} should be retained across multiple calls
     *   (useful for looping animations).
     */
    protected EventBasedPredicate(boolean retainEventResult) { toggle = retainEventResult; }

    /**
     * Determines from the passed event whether the entity specified in the event should be animated.
     * The default implementation returns true.
     */
    protected boolean shouldAnimate(E event) { return true; }

    /** Fallback for when event handling is not available. The default implementation returns false. */
    protected boolean multiplayerFallback(T entity, EntityInfo info) { return false; }

    /**
     * Get the entity that this predicate should be associated with. This entity is
     * the entity that will be animated. The default implementation returns {@code event.getEntity()}.
     */
    protected Entity getAssociatedEntity(E event) { return event.getEntity(); }

    /**
     * The actual method subscribed to the event bus. This method is package-private because private methods
     * do not work with the event system. The actual event passed to this method will always be a subtype of E
     * because the class of E is passed to the event bus. However, this forwards the raw type of IGenericEvents.
     * This should not be an issue with EntityEvents.
     */
    @SubscribeEvent
    final void onEventWrap(E event) {

        assert eventType.isInstance(event) : event.getClass();
        if(shouldAnimate(event))
            entitiesToAnimate.add(getAssociatedEntity(event));
        else if(toggle)
            entitiesToAnimate.remove(getAssociatedEntity(event));
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

            return toggle ? entitiesToAnimate.contains(entity) : entitiesToAnimate.remove(entity);
        }
    }
}