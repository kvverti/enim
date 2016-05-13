package kvverti.enim.entity;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.modelsystem.ModelElement;
import kvverti.enim.modelsystem.EntityState;
import kvverti.enim.modelsystem.Keys;

public class ENIMTileEntityRender<T extends TileEntity> extends TileEntitySpecialRenderer<T> implements ReloadableRender {

	protected final Map<String, EntityState> states;
	private final ResourceLocation entityStateFile;

	public ENIMTileEntityRender(String modDomain, String entityStateName) {

		entityStateFile = new ResourceLocation(modDomain, Keys.STATES_DIR + entityStateName + Keys.JSON);
		states = new HashMap<>();
		getEntityStateNames().forEach(s -> states.put(s, new EntityState(s)));
		ReloadableRender.renders.add(this);
	}

	@Override
	public final ResourceLocation getEntityStateFile() {

		return entityStateFile;
	}

	@Override
	public Set<String> getEntityStateNames() {

		Set<String> s = new HashSet<>();
		s.add(Keys.STATE_NORMAL);
		return s;
	}

	@Override
	public final void renderTileEntityAt(T tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x + 0.5f, (float) y, (float) z + 0.5f);
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);

		EntityState state = getStateFromTile(tileEntity);
		ENIMModel model = state.model();
		bindTexture(getTileTexture(tileEntity));
		GlStateManager.rotate(state.rotation(), 0.0f, 1.0f, 0.0f);
		preRender(tileEntity, x, y, z, partialTicks, destroyStage);
		model.render(tileEntity, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f * state.scale());
		postRender(tileEntity);
		GlStateManager.popMatrix();
	}

	public void preRender(T tile, double x, double y, double z, float partialTicks, int destroyStage) { }

	public void postRender(T tile) { }

	public EntityState getStateFromTile(T tile) {

		return states.get(Keys.STATE_NORMAL);
	}

	protected final ResourceLocation getTileTexture(T tile) {

		return getStateFromTile(tile).texture();
	}

	@Override
	public final void reloadRender(EntityState state) {

		if(getEntityStateNames().contains(state.name())) {

			EntityState realState = states.get(state.name());
			realState.reloadState(state);
			realState.model().textureWidth = state.xSize();
			realState.model().textureHeight = state.ySize();
		}
	}

	@Override
	public final void setMissingno() {

		states.values().forEach(state -> state.model().setMissingno());
	}
}