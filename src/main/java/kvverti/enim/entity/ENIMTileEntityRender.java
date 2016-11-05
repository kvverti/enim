package kvverti.enim.entity;

import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableSet;

import kvverti.enim.entity.state.RenderState;
import kvverti.enim.entity.state.StateManager;
import kvverti.enim.model.EntityState;
import kvverti.enim.model.EntityStateMap;
import kvverti.enim.Keys;

public abstract class ENIMTileEntityRender<T extends TileEntity> extends TileEntitySpecialRenderer<T> implements ReloadableRender {

	private final ResourceLocation entityStateFile;
	private final StateManager stateManager;
	private EntityState currentState;

	protected ENIMTileEntityRender(String modDomain, String entityStateFile, IProperty<?>... properties) {

		this.entityStateFile = new ResourceLocation(modDomain, Keys.STATES_DIR + entityStateFile + Keys.JSON);
		this.stateManager = new StateManager(properties);
	}

	public abstract RenderState getStateFromTile(T tile);

	protected final StateManager getStateManager() {

		return stateManager;
	}

	protected final EntityState getCurrentEntityState() {

		return currentState;
	}

	@Override
	public final ResourceLocation getEntityStateFile() {

		return entityStateFile;
	}

	@Override
	public final ImmutableSet<String> getEntityStateNames() {

		return stateManager.stateStringNames();
	}

	@Override
	public final void renderTileEntityAt(T tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x + 0.5f, (float) y, (float) z + 0.5f);
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);

		RenderState renderState = getStateFromTile(tileEntity);
		currentState = stateManager.getState(renderState);
		ENIMModel model = stateManager.getModel(renderState);
		bindTexture(currentState.texture());
		GlStateManager.rotate(currentState.y(), 0.0f, 1.0f, 0.0f);
		EntityInfo info = new EntityInfo();
		info.partialTicks = partialTicks;
		info.scale = 0.0625f * currentState.scale();
		preRender(tileEntity, info);
		model.render(tileEntity, info);
		postRender(tileEntity, info);
		GlStateManager.popMatrix();
	}

	public void preRender(T tile, EntityInfo info) { }

	public void postRender(T tile, EntityInfo info) { }

	@Override
	public final void reload(EntityStateMap states) {

		stateManager.reloadStates(states);
	}

	@Override
	public final void setMissingno() {

		stateManager.setAllInvalid();
	}
}