package kvverti.enim.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntitySquid;

public class SquidRender extends BasicLivingRender<EntitySquid> {

	public SquidRender(RenderManager manager) {

		super(manager);
	}

	@Override
	protected void preRender(EntitySquid entity, EntityInfo info) {

		super.preRender(entity, info);
		info.entityPitch = -Entities.interpolate(entity.prevSquidPitch, entity.squidPitch, info.partialTicks);
	}
}