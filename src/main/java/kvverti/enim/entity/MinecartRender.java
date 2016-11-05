package kvverti.enim.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityMinecart;

public class MinecartRender extends BasicRender<EntityMinecart> {

	public MinecartRender(RenderManager manager, String modDomain, String entityStateName) {

		super(manager, modDomain, entityStateName);
	}

	@Override
	public void preRender(EntityMinecart entity, EntityInfo info) {

		GlStateManager.rotate(-info.entityYaw, 0.0f, 1.0f, 0.0f);
		double dx = entity.posX - entity.lastTickPosX;
		double dy = entity.posY - entity.lastTickPosY;
		double dz = entity.posZ - entity.lastTickPosZ;

		double newYaw = Math.atan2(dz, dx) * 180.0 / Math.PI;
		entity.rotationYaw = newYaw == 0.0 ? entity.prevRotationYaw : (float) newYaw;
		GlStateManager.rotate(entity.rotationYaw, 0.0f, 1.0f, 0.0f);

		double dydx = +Math.abs(dy / dx);
		double dydz = -Math.abs(dy / dz);
		boolean dxGreater = Math.abs(dx) > Math.abs(dz);
		double newPitch = Math.atan(dxGreater ? dydx : dydz) * 180.0 / Math.PI;
		if(Double.isNaN(newPitch)) newPitch = entity.prevRotationPitch;
		if(dxGreater && dy < 0 || !dxGreater && dy > 0) newPitch = -newPitch;
		entity.rotationPitch = (float) newPitch;
		GlStateManager.rotate((float) -newPitch, 0.0f, 0.0f, 1.0f);
	}
}