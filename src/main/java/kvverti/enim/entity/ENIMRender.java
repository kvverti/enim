package kvverti.enim.entity;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.modelsystem.ModelElement;

public class ENIMRender<T extends Entity> extends Render<T> {

//	protected final RenderManager renderManager;

	public static final List<ENIMRender> renders = new ArrayList<>();

	private ResourceLocation texture;
	private final ENIMModel model;

	public ENIMRender(ENIMModel model) {

		super(Minecraft.getMinecraft().getRenderManager());
		this.model = model;
		renders.add(this);
	}

	@Override
	public void doRender(T entity, double x, double y, double z, float yaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
		bindEntityTexture(entity);
		model.render(entity, 0.0f, 0.0f, 0.0f, yaw, 0.0f, 0.0625f);
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, yaw, partialTicks);
	}

	@Override
	protected ResourceLocation getEntityTexture(T entity) {

		//TODO
		return new ResourceLocation("enim", "textures/entity/test_entity.png");
	}

	public void reloadRender(Set<ModelElement> elements) {

		//TODO
		model.reloadModel(elements);
	}
}