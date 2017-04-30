package kvverti.enim.entity;

import net.minecraft.block.properties.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;

import kvverti.enim.Vec3f;
import kvverti.enim.entity.color.CustomDyeColor;
import kvverti.enim.entity.color.MinecraftCustomDyeColors;
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

		final CustomDyeColor sheepColors = MinecraftCustomDyeColors.DEFAULT;
		if(entity.hasCustomName() && "jeb_".equals(entity.getCustomNameTag())) {

			final int speed = 25;
			int len = EnumDyeColor.values().length;
			int index1 = (entity.ticksExisted / speed + entity.getEntityId()) % len;
			int index2 = (index1 + 1) % len;
			float partial = (entity.ticksExisted % speed + info.partialTicks) / speed;
			Vec3f color1 = sheepColors.getColor(EnumDyeColor.byMetadata(index1));
			Vec3f color2 = sheepColors.getColor(EnumDyeColor.byMetadata(index2));
			return color1.interpolate(color2, partial);
		}
		return sheepColors.getColor(entity.getFleeceColor());
	}
}