package kvverti.enim.entity;

import java.util.Set;
import java.util.HashSet;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityCreeper;

import kvverti.enim.entity.state.RenderState;

public class CreeperRender extends LivingRender<EntityCreeper> {

	public CreeperRender(RenderManager manager, String modDomain, String entityStateFile) {

		super(manager, modDomain, entityStateFile);
	}

	@Override
	public RenderState getStateFromEntity(EntityCreeper entity) {

		return getStateManager().getDefaultState();
	}
}