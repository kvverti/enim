package kvverti.enim.entity;

import java.util.Set;
import java.util.HashSet;

import net.minecraft.block.properties.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

import kvverti.enim.entity.state.RenderState;
import kvverti.enim.modelsystem.Keys;

public class BasicRender<T extends Entity> extends ENIMRender<T> {

	public BasicRender(RenderManager manager, String modDomain, String entityStateFile) {

		super(manager, modDomain, entityStateFile);
	}

	@Override
	public RenderState getStateFromEntity(T entity) {

		return getStateManager().getDefaultState();
	}
}