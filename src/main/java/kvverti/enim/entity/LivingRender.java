package kvverti.enim.entity;

import net.minecraft.block.properties.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.TextFormatting;

public abstract class LivingRender<T extends EntityLivingBase> extends ENIMRender<T> {

	public static final float NAMETAG_VISIBILITY_RANGE_SQ = 64.0f * 64.0f;
	public static final IProperty<Boolean> BABY = PropertyBool.create("baby");

	protected LivingRender(RenderManager manager, IProperty<?>... properties) {

		super(manager, properties);
	}

	@Override
	public boolean shouldRender(T entity) {

		return !entity.isInvisible() || !entity.isInvisibleToPlayer(Entities.thePlayer());
	}

	/* Must call super.preRender(entity, state, info); in subclasses!! */
	@Override
	protected void preRender(T entity, EntityInfo info) {

		super.preRender(entity, info);
		//fall over when dead
		if(entity.deathTime > 0)
			rotateCorpse(entity);
		//tint red when damaged
		if(entity.hurtTime > 0 || entity.deathTime > 0)
			GlStateManager.color(1.0f, 0.5f, 0.5f);
		//invisible mobs as translucent to players in creative/spectator
		if(entity.isInvisible() && !entity.isInvisibleToPlayer(Entities.thePlayer())) {

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(770, 771);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 0.25f);
		}
		//"Dinnerbone" or "Grumm" mobs render upside down
		if(entity.hasCustomName()) {

			String name = TextFormatting.getTextWithoutFormattingCodes(entity.getName());
			if("Grumm".equals(name) || "Dinnerbone".equals(name)) {

				GlStateManager.translate(0.0f, -entity.height, 0.0f);
				GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
			}
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
			entity != renderManager.renderViewEntity &&
			!entity.isInvisibleToPlayer(Entities.thePlayer()) &&
			entity.getPassengers().isEmpty();
	}

	@Override
	public void renderName(T entity, double x, double y, double z) {

		if(canRenderName(entity)) {

			double distanceSq = entity.getDistanceSqToEntity(renderManager.renderViewEntity);
			if(distanceSq < NAMETAG_VISIBILITY_RANGE_SQ) {

				float namePos = getCurrentEntityState().model().properties().nameplate();
				float scale = 0.0625f * getCurrentEntityState().scale();
				EntityRenderer.drawNameplate(getFontRendererFromRenderManager(),
					entity.getDisplayName().getFormattedText(),
					(float) x,
					(float) y + namePos * scale + (3.0f / 16.0f),
					(float) z,
					0,
					renderManager.playerViewY,
					renderManager.playerViewX,
					renderManager.options.thirdPersonView == 2,
					entity.isSneaking());
			}
		}
	}
}