package kvverti.enim.entity;

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableList;

import kvverti.enim.entity.state.RenderState;
import kvverti.enim.model.EntityStateMap;

/**
 * A renderer that can be reloaded from the game resources. These renderers can be changed via resource packs. ENIM uses
 * {@code ReloadableRender}s for {@link net.minecraft.entity.Entity} and {@link net.minecraft.tileentity.TileEntity} renderers,
 * but other types of renderers may also be made reloadable. {@code ReloadableRender}s use instances of
 * {@link kvverti.enim.entity.state.RenderState} and {@link kvverti.enim.model.EntityState} to control their rendering properties.
 * @see kvverti.enim.entity.state.RenderState
 * @see kvverti.enim.entity.state.StateManager
 */
public interface ReloadableRender {

	/**
	 * Returns the list of valid states for this render
	 */
	ImmutableList<RenderState> getValidStates();

	/**
	 * Reloads this render from the game resources. This enables the render's models to be changed while the game is running.
	 * @param states the state replacements for this render
	 */
	void reload(EntityStateMap states);

	/**
	 * Sets this render to a default state not dependent on the game resources. Use this method when the game resources this
	 * render relies on are missing or invalid.
	 */
	void setMissingno();
}