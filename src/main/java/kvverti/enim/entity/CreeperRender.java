package kvverti.enim.entity;

import java.util.Set;
import java.util.HashSet;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityCreeper;

import kvverti.enim.modelsystem.EntityState;
import kvverti.enim.modelsystem.Keys;

public class CreeperRender extends LivingRender<EntityCreeper> {

	public CreeperRender(RenderManager manager, String modDomain, String entityStateFile) {

		super(manager, modDomain, entityStateFile, Keys.STATE_NORMAL);
	}

	@Override
	public EntityState getStateFromEntity(EntityCreeper entity) {

		return getState(Keys.STATE_NORMAL);
	}
}