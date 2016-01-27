package kvverti.enim.entity;

import java.io.InputStream;
import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.modelsystem.ModelElement;
import kvverti.enim.modelsystem.Texture;
import kvverti.enim.Logger;

public class ENIMRender<T extends Entity> extends Render<T> {

//	protected final RenderManager renderManager;

	public static final List<ENIMRender<?>> renders = new ArrayList<>();
	private static final TextureManager texManager = Minecraft.getMinecraft().getTextureManager();

	private final ResourceLocation entityStateFile;
	private final List<ResourceLocation> textures = new ArrayList<>();
	private final ENIMModel model;

	public ENIMRender(String modDomain, String entityStateName, ENIMModel model) {

		super(Minecraft.getMinecraft().getRenderManager());
		entityStateFile = new ResourceLocation(modDomain, "entitystates/" + entityStateName + ".json");
		this.model = model;
		renders.add(this);
	}

	public final ResourceLocation getEntityStateFile() {

		return entityStateFile;
	}

	private void setTextures(List<Texture> list) {

		textures.clear();
		for(Texture t : list) {

			textures.add(bind(t.getTexture()));
		}
		model.textureWidth = list.get(0).getXSize();
		model.textureHeight = list.get(0).getYSize();
		if(model.textureWidth < 64 || model.textureHeight < 32)
			Logger.warn("Texture dimensions are less than the minimum 64x32");
	}

	private ResourceLocation bind(ResourceLocation loc) {

		try(InputStream istream = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream()) {

			ResourceLocation tex = texManager.getDynamicTextureLocation(
				"enim_entity_texture", new DynamicTexture(ImageIO.read(istream)));
			texManager.bindTexture(tex);
			return tex;

		} catch(IOException e) {

			Logger.error("Could not bind texture for " + loc);
			return loc;
		}
	}

	@Override
	public void doRender(T entity, double x, double y, double z, float yaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
		GlStateManager.rotate(yaw, 0.0f, 1.0f, 0.0f);
		bindEntityTexture(entity);
		model.render(entity, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f);
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, yaw, partialTicks);
	}

	@Override
	protected ResourceLocation getEntityTexture(T entity) {

		return textures.get(EntityRandomCounters.get(entity) % textures.size());
	}

	public final void reloadRender(Set<ModelElement> elements, List<Texture> textures) {

		model.reloadModel(elements);
		setTextures(textures);
	}

	public final void setMissingno() {

		model.setMissingno();
		textures.clear();
		textures.add(new ResourceLocation("missingno"));
	}
}