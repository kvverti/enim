package kvverti.enim.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityBoat;

public class BoatRender extends BasicRender<EntityBoat> {

	public BoatRender(RenderManager manager, String modDomain, String entityStateFile) {

		super(manager, modDomain, entityStateFile);
	}

	/** Modified from RenderBoat#doRender */
	@Override
	protected void preRender(EntityBoat entity, EntityInfo info) {

		super.preRender(entity, info);
		float roll = (float) entity.getTimeSinceHit() - info.partialTicks;
		float damage = Math.max(0.0f, entity.getDamageTaken() - info.partialTicks);
		if(roll > 0.0f)
			GlStateManager.rotate((float) Math.sin(roll) * roll * (damage / 10.0f) * (float) entity.getForwardDirection(), 1.0f, 0.0f, 0.0f);
	}
}