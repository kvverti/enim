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
import kvverti.enim.Logger;

public class ENIMTileEntityRender<T extends TileEntity> extends TileEntitySpecialRenderer<T> implements ReloadableRender {

	protected final Map<String, EntityState> states;
	private final ResourceLocation entityStateFile;

	public ENIMTileEntityRender(String modDomain, String entityStateName) {

		entityStateFile = new ResourceLocation(modDomain, "entitystates/" + entityStateName + ".json");
		states = new HashMap<>();
		for(String s : getEntityStateNames()) {

			states.put(s, new EntityState(s));
		}
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
		ENIMModel model = state.getModel();
		bindTexture(getTileTexture(tileEntity));
		float[] rots = state.getRotation();
		GlStateManager.rotate(+rots[2], 0.0f, 0.0f, 1.0f);
		GlStateManager.rotate(+rots[1], 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(-rots[0], 1.0f, 0.0f, 0.0f);
		preRender(tileEntity);
		model.render(null, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f * state.getScale()); //tile specific version?
		postRender(tileEntity);
		GlStateManager.popMatrix();
	}

	public void preRender(T tile) { }

	public void postRender(T tile) { }

	public EntityState getStateFromTile(T tile) {

		return states.get(Keys.STATE_NORMAL);
	}

	protected final ResourceLocation getTileTexture(T tile) {

		EntityState state = getStateFromTile(tile);
		return state.getTexture();
	}

	@Override
	public final void reloadRender(EntityState state) {

		if(getEntityStateNames().contains(state.getName())) {

			EntityState realState = states.get(state.getName());
			realState.reloadState(state);
			realState.getModel().textureWidth = state.getXSize();
			realState.getModel().textureHeight = state.getYSize();
		}
	}

	@Override
	public final void setMissingno() {

		for(EntityState s : states.values()) {

			s.getModel().setMissingno();
		}
	}
}