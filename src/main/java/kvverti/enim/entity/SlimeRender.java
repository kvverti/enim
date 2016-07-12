package kvverti.enim.entity;

import java.util.Set;
import java.util.HashSet;

import net.minecraft.block.properties.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntitySlime;

import org.lwjgl.opengl.GL11;

import kvverti.enim.entity.state.RenderState;
import kvverti.enim.modelsystem.EntityState;

public class SlimeRender extends LivingRender<EntitySlime> {

	public static final IProperty<Integer> SLIME_SIZE = PropertyInteger.create("size", 0, 3);

	public SlimeRender(RenderManager manager, String modDomain, String entityStateFile) {

		super(manager, modDomain, entityStateFile, SLIME_SIZE);
	}

	@Override
	public RenderState getStateFromEntity(EntitySlime entity) {

		int size = entity.getSlimeSize() - 1;
		if     (size > 3) size = 3;
		else if(size < 0) size = 0;
		return getStateManager().getDefaultState().withProperty(SLIME_SIZE, size);
	}

	@Override
	public void preRender(EntitySlime entity, EntityState state, float yaw) {

		super.preRender(entity, state, yaw);
		float squish = 2.0f / (2.0f + entity.squishFactor);
		GlStateManager.scale(squish, 1.0f / squish, squish);
	}
}