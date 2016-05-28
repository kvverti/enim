package kvverti.enim.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;

import kvverti.enim.modelsystem.EntityState;

public class LivingRender<T extends EntityLivingBase> extends ENIMRender<T> {

	public LivingRender(RenderManager manager, String modDomain, String entityStateFile) {

		super(manager, modDomain, entityStateFile);
	}

	@Override
	public boolean shouldRender(T entity) {

		return !entity.isInvisible() || !entity.isInvisibleToPlayer(Entities.thePlayer());
	}

	/* Must call super.preRender(entity, state, yaw); in subclasses!! */
	@Override
	public void preRender(T entity, EntityState state, float yaw) {

		if(entity.deathTime > 0)
			rotateCorpse(entity);
		if(entity.hurtTime > 0 || entity.deathTime > 0) {

			GlStateManager.color(1.0f, 0.5f, 0.5f);
		}
		if(entity.isInvisible() && !entity.isInvisibleToPlayer(Entities.thePlayer())) {

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(770, 771);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 0.15f);
		}
	}

	protected void rotateCorpse(T entity) {

		final float time = 15.0f; //ticks
		float rot = Entities.interpolate(0.0f, 90.0f, (float) entity.deathTime / time);
		GlStateManager.rotate(Math.min(rot, 90.0f), 0.0f, 0.0f, 1.0f);
	}
}