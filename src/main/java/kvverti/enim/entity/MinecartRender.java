package kvverti.enim.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.math.Vec3d;

public class MinecartRender extends BasicRender<EntityMinecart> {

	public MinecartRender(RenderManager manager) {

		super(manager);
	}

	/** Modified from RenderMinecart#doRender */
	@Override
	protected void preRender(EntityMinecart entity, EntityInfo info) {

		super.preRender(entity, info);
		GlStateManager.rotate(-info.entityYaw, 0.0f, 1.0f, 0.0f);
		double dx = Entities.interpolate((float) entity.lastTickPosX, (float) entity.posX, info.partialTicks);
		double dy = Entities.interpolate((float) entity.lastTickPosY, (float) entity.posY, info.partialTicks);
		double dz = Entities.interpolate((float) entity.lastTickPosZ, (float) entity.posZ, info.partialTicks);
		Vec3d fallback = entity.getPos(dx, dy, dz);
		if(fallback != null) {

			final double MAGIC = 0.3;
			Vec3d weightedMax = entity.getPosOffset(dx, dy, dz, MAGIC);
			Vec3d weightedMin = entity.getPosOffset(dx, dy, dz, -MAGIC);
			if(weightedMax == null)
				weightedMax = fallback;
			if(weightedMin == null)
				weightedMin = fallback;
			Vec3d rotation = weightedMin.addVector(-weightedMax.x, -weightedMax.y, -weightedMax.z);
			if(rotation.lengthVector() != 0.0) {

				info.entityYaw = Entities.toDegrees((float) Math.atan2(rotation.z, rotation.x));
				info.entityPitch = (float) (Math.atan(rotation.y) * 73.0);
			}
		}
		GlStateManager.rotate(info.entityYaw, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(-info.entityPitch, 0.0f, 0.0f, 1.0f);
		float roll = (float) entity.getRollingAmplitude() - info.partialTicks;
		float damage = Math.max(0.0f, entity.getDamage() - info.partialTicks);
		if(roll > 0.0f)
			GlStateManager.rotate((float) Math.sin(roll) * roll * (damage / 10.0f) * (float) entity.getRollingDirection(), 1.0f, 0.0f, 0.0f);
	}
}