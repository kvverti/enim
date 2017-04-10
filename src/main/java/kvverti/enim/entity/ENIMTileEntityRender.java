package kvverti.enim.entity;

import java.util.List;

import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableSet;

import kvverti.enim.entity.state.RenderState;
import kvverti.enim.entity.state.StateManager;
import kvverti.enim.model.EntityState;
import kvverti.enim.model.EntityStateMap;
import kvverti.enim.Keys;

public abstract class ENIMTileEntityRender<T extends TileEntity> extends TileEntitySpecialRenderer<T> implements ReloadableRender {

	private final StateManager stateManager;
	private EntityState currentState;

	protected ENIMTileEntityRender(IProperty<?>... properties) {

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
		List<EntityState> layers = currentState.getLayers();
		for(int i = 0; i < layers.size(); i++)
			renderLayer(tileEntity, info, layers.get(i), stateManager.getLayerModel(renderState, i));
		postRender(tileEntity, info);
		GlStateManager.popMatrix();
	}

	private void renderLayer(T tile, EntityInfo info, EntityState layer, ENIMModel model) {

		GlStateManager.pushMatrix();
		bindTexture(layer.texture());
		GlStateManager.rotate(layer.y(), 0.0f, 1.0f, 0.0f);
		info.scale = 0.0625f * layer.scale();
		model.render(tile, info);
		GlStateManager.popMatrix();
	}

	private void renderOverlay(T tile, EntityInfo info, ENIMModel model, ResourceLocation overlay) {

		bindTexture(overlay);
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.blendFunc(1, 1);
		//begin magic
		GlStateManager.depthMask(true);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0xf0f0, 0.0f);
		//end magic
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		model.render(tile, info);
		//begin magic
		net.minecraft.util.math.BlockPos pos = tile.getPos();
		net.minecraft.world.World world = tile.getWorld();
		int brightness = world != null && world.isBlockLoaded(pos) ?
			world.getCombinedLight(pos, 0)
			: 0;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightness % 0x10000, brightness / 0x10000);
		//end magic
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
	}

	/**
	 * Method called immediately before the main model is rendered.
	 */
	protected void preRender(T tile, EntityInfo info) { }

	/**
	 * Method called immediately after the main model and any layers are rendered.
	 */
	protected void postRender(T tile, EntityInfo info) { }

	/**
	 * Returns the color in RGB format that will be overlayed (multiplied) onto applicable model elements.
	 */
	public int getColorOverlay(T tile, int colorIndex) {

		return 0xffffff;
	}

	@Override
	public final void reload(EntityStateMap states) {

		stateManager.reloadStates(states);
	}

	@Override
	public final void setMissingno() {

		stateManager.setAllInvalid();
	}
}