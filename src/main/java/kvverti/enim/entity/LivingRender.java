package kvverti.enim.entity;

import net.minecraft.block.properties.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;

import kvverti.enim.modelsystem.EntityState;

public abstract class LivingRender<T extends EntityLivingBase> extends ENIMRender<T> {

	public static final float NAMETAG_VISIBILITY_RANGE_SQ = 64.0f * 64.0f;
	public static final IProperty<Boolean> BABY = PropertyBool.create("baby");

	protected LivingRender(RenderManager manager, String modDomain, String entityStateFile, IProperty<?>... properties) {

		super(manager, modDomain, entityStateFile, properties);
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
		if(entity.hurtTime > 0 || entity.deathTime > 0)
			GlStateManager.color(1.0f, 0.5f, 0.5f);
		if(entity.isInvisible() && !entity.isInvisibleToPlayer(Entities.thePlayer())) {

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(770, 771);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 0.25f);
		}
	}

	protected void rotateCorpse(T entity) {

		final float time = 15.0f; //ticks
		float rot = Entities.interpolate(0.0f, 90.0f, (float) entity.deathTime / time);
		GlStateManager.rotate(Math.min(rot, 90.0f), 0.0f, 0.0f, 1.0f);
	}

	@Override
	protected boolean canRenderName(T entity) {

		return entity.hasCustomName() &&
			Minecraft.isGuiEnabled() &&
			entity != renderManager.livingPlayer &&
			!entity.isInvisibleToPlayer(Entities.thePlayer()) &&
			entity.riddenByEntity == null;
	}

	@Override
	public void renderName(T entity, double x, double y, double z) {

		if(canRenderName(entity)) {

			double distanceSq = entity.getDistanceSqToEntity(renderManager.livingPlayer);
			if(distanceSq < NAMETAG_VISIBILITY_RANGE_SQ) {

				renderOffsetLivingLabel(entity,
					x,
					y - (entity.isChild() ? entity.height / 2.0 : 0.0),
					z,
					entity.getDisplayName().getFormattedText(),
					2.0f / 75.0f,
					distanceSq);
			}
		}
	}
}