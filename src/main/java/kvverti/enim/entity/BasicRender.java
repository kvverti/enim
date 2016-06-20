package kvverti.enim.entity;

import java.util.Set;
import java.util.HashSet;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

import kvverti.enim.modelsystem.EntityState;
import kvverti.enim.modelsystem.Keys;

public class BasicRender<T extends Entity> extends ENIMRender<T> {

	public BasicRender(RenderManager manager, String modDomain, String entityStateFile) {

		super(manager, modDomain, entityStateFile, Keys.STATE_NORMAL);
	}

	@Override
	public EntityState getStateFromEntity(T entity) {

		return getState(Keys.STATE_NORMAL);
	}
}