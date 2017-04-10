package kvverti.enim.entity;

import net.minecraft.block.properties.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;

import kvverti.enim.Vec3f;
import kvverti.enim.entity.state.RenderState;

public class SheepRender extends LivingRender<EntitySheep> {

	public static final IProperty<Boolean> SHORN = PropertyBool.create("shorn");

	public SheepRender(RenderManager manager) {

		super(manager, BABY, SHORN);
	}

	@Override
	public RenderState getStateFromEntity(EntitySheep entity) {

		return getStateManager().getDefaultState()
			.withProperty(BABY, entity.isChild())
			.withProperty(SHORN, entity.getSheared());
	}

	@Override
	public Vec3f getColorOverlay(EntitySheep entity, EntityInfo info, int colorIndex) {

		//jeb!
		if(entity.hasCustomName() && "jeb_".equals(entity.getCustomNameTag())) {

			final int speed = 25;
			int len = EnumDyeColor.values().length;
			int index1 = (entity.ticksExisted / speed + entity.getEntityId()) % len;
			int index2 = (index1 + 1) % len;
			float partial = (entity.ticksExisted % speed + info.partialTicks) / speed;
			float[] color1 = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(index1));
			float[] color2 = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(index2));
			return Vec3f.of(Entities.interpolate(color1[0], color2[0], partial),
				Entities.interpolate(color1[1], color2[1], partial),
				Entities.interpolate(color1[2], color2[2], partial));
		}
		float[] color = EntitySheep.getDyeRgb(entity.getFleeceColor());
		return Vec3f.of(color[0], color[1], color[2]);
	}
}