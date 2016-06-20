package kvverti.enim.entity;

import java.util.Set;
import java.util.HashSet;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntitySlime;

import org.lwjgl.opengl.GL11;

import kvverti.enim.modelsystem.EntityState;

public class SlimeRender extends LivingRender<EntitySlime> {


	public SlimeRender(RenderManager manager, String modDomain, String entityStateFile) {

		super(manager, modDomain, entityStateFile, "size_0", "size_1", "size_2", "size_3");
	}

	@Override
	public EntityState getStateFromEntity(EntitySlime entity) {

		EntityState state = getState("size_" + (entity.getSlimeSize() - 1));
		if(state == null) state = getState("size_3");
		return state;
	}

	@Override
	public void preRender(EntitySlime entity, EntityState state, float yaw) {

		super.preRender(entity, state, yaw);
		float squish = 2.0f / (2.0f + entity.squishFactor);
		GlStateManager.scale(squish, 1.0f / squish, squish);
	}
}