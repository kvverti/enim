package kvverti.enim.entity;

import java.io.InputStream;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.modelsystem.ModelElement;
import kvverti.enim.modelsystem.EntityState;
import kvverti.enim.modelsystem.Keys;
import kvverti.enim.Logger;

public class ENIMRender<T extends Entity> extends Render<T> implements ReloadableRender {

//	protected final RenderManager renderManager;

	protected final Map<String, EntityState> states;
	private final ResourceLocation entityStateFile;

	public ENIMRender(RenderManager manager, String modDomain, String entityStateName) {

		super(manager);
		entityStateFile = new ResourceLocation(modDomain, Keys.STATES_DIR + entityStateName + Keys.JSON);
		states = new HashMap<>();
		getEntityStateNames().forEach(s -> states.put(s, new EntityState(s)));
		ReloadableRender.renders.add(this);
	}

	private static TextureManager texManager() {

		return Minecraft.getMinecraft().getTextureManager();
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

	private ResourceLocation bind(ResourceLocation loc) {

		try(InputStream istream = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream()) {

			ResourceLocation tex = texManager().getDynamicTextureLocation(
				"enim_entity_texture", new DynamicTexture(ImageIO.read(istream)));
			texManager().bindTexture(tex);
			return tex;

		} catch(IOException e) {

			Logger.error("Could not bind texture for " + loc);
			return loc;
		}
	}

	@Override
	public final void doRender(T entity, double x, double y, double z, float yaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
		GlStateManager.rotate(yaw, 0.0f, 1.0f, 0.0f);

		EntityState state = getStateFromEntity(entity);
		ENIMModel model = state.model();
		bindEntityTexture(entity);
		float[] rots = state.rotation();
		GlStateManager.rotate(+rots[2], 0.0f, 0.0f, 1.0f);
		GlStateManager.rotate(+rots[1], 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(-rots[0], 1.0f, 0.0f, 0.0f);
		preRender(entity, x, y, z, yaw, partialTicks);
		model.render(entity, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f * state.scale());
		postRender(entity);
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, yaw, partialTicks);
	}

	public void preRender(T entity, double x, double y, double z, float yaw, float partialTicks) { }

	public void postRender(T entity) { }

	public EntityState getStateFromEntity(T entity) {

		return states.get(Keys.STATE_NORMAL);
	}

	@Override
	protected final ResourceLocation getEntityTexture(T entity) {

		EntityState state = getStateFromEntity(entity);
		ResourceLocation result = bind(state.texture());
		return result;
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