package kvverti.enim.entity;

import java.util.Set;

import net.minecraft.util.ResourceLocation;

import kvverti.enim.modelsystem.EntityState;

/**
 * A renderer that can be reloaded from the game resources. These renderers can be changed via resource packs. ENIM uses
 * {@code ReloadableRender}s for {@link net.minecraft.entity.Entity} and {@link net.minecraft.tileentity.TileEntity} renderers,
 * but other types of renderers may also be made reloadable. {@code ReloadableRender}s use instances of
 * {@link kvverti.enim.entity.state.RenderState} and {@link EntityState} to control their rendering properties.
 * @see kvverti.enim.entity.state.RenderState
 * @see kvverti.enim.entity.state.StateManager
 */
public interface ReloadableRender {

	/**
	 * Returns the file that associates the client models with render states. This file is in the JSON format and follows the
	 * requirements for an {@code entitystate} file.
	 * @return the location of this render's entity state file
	 */
	ResourceLocation getEntityStateFile();

	/**
	 * Returns the set of state names for this render. These are a fixed set of state names that are used as the keys in the
	 * {@code entitystate} file.
	 * @return the set of state names for thsi render
	 */
	Set<String> getEntityStateNames();

	/**
	 * Reloads this render from the game resources. This enables the render's models to be changed while the game is running.
	 * @param state the properties with which to replace a particular state
	 */
	void reloadRender(EntityState state);

	/**
	 * Sets this render to a default state not dependent on the game resources. Use this method when the game resources this
	 * render relies on are missing or invalid.
	 */
	void setMissingno();
}