package kvverti.enim.entity.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

import kvverti.enim.entity.EntityInfo;

/** A function which returns whether an entity should animate given its circumstances. */
@FunctionalInterface
public interface AnimPredicate<T> {

	boolean shouldAnimate(T entity, EntityInfo info);

	static <T> AnimPredicate<T> alwaysTrue() { return (e, i) -> true; }

	static <T> AnimPredicate<T> alwaysFalse() { return (e, i) -> false; }

	/** AnimPredicate that uses different computations depending on whether the integrated server is avaliable. */
	abstract class ServerDependentPredicate<T> implements AnimPredicate<T> {

		/** Sole constructor, for use by (usually anonymous) subclasses */
		protected ServerDependentPredicate() { }

		/**
		 * Returns whether the entity should be animated given the integrated server is avaliable. This allows the use of
		 * Forge event listeners and other entity properties that are not avaliable on the pure client.
		 */
		protected abstract boolean computeSingleplayer(T entity, EntityInfo info);

		/**
		 * Returns whether the entity should be animated given the integrated server is <em>not</em> avaliable.
		 * Client Forge event listeners will not fire, and entity properties may be unreliable.
		 */
		protected abstract boolean computeMultiplayer(T entity, EntityInfo info);

		@Override
		public final boolean shouldAnimate(T entity, EntityInfo info) {

			return isClientIntegrated() ? computeSingleplayer(entity, info) : computeMultiplayer(entity, info);
		}

		private boolean isClientIntegrated() {

			return Minecraft.getMinecraft().isSingleplayer();
		}
	}
}