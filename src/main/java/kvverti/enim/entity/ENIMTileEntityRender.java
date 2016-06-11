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

public abstract class ENIMTileEntityRender<T extends TileEntity> extends TileEntitySpecialRenderer<T> implements ReloadableRender {

	private final Map<String, EntityState> states;
	private final ResourceLocation entityStateFile;

	protected ENIMTileEntityRender(String modDomain, String entityStateFile, String... stateNames) {

		this.entityStateFile = new ResourceLocation(modDomain, Keys.STATES_DIR + entityStateFile + Keys.JSON);
		states = new HashMap<>();
		for(String s : stateNames) { states.put(s, new EntityState(s)); }
		ReloadableRender.renders.add(this);
	}

	public abstract EntityState getStateFromTile(T tile);

	@Override
	public final ResourceLocation getEntityStateFile() {

		return entityStateFile;
	}

	protected EntityState getState(String name) {

		return states.get(name);
	}

	@Override
	public final Set<String> getEntityStateNames() {

		return new HashSet<>(states.keySet());
	}

	@Override
	public final void renderTileEntityAt(T tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x + 0.5f, (float) y, (float) z + 0.5f);
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);

		EntityState state = getStateFromTile(tileEntity);
		ENIMModel model = state.model();
		bindTexture(state.texture());
		GlStateManager.rotate(state.rotation(), 0.0f, 1.0f, 0.0f);
		preRender(tileEntity, state);
		model.render(tileEntity, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f * state.scale());
		postRender(tileEntity);
		GlStateManager.popMatrix();
	}

	public void preRender(T tile, EntityState state) { }

	public void postRender(T tile) { }

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