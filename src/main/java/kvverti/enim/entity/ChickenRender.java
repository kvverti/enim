package kvverti.enim.entity;

import java.util.Set;
import java.util.HashSet;

import net.minecraft.block.properties.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityChicken;

import kvverti.enim.entity.state.RenderState;
import kvverti.enim.modelsystem.Keys;

public class ChickenRender extends LivingRender<EntityChicken> {

	public ChickenRender(RenderManager manager, String modDomain, String entityStateFile) {

		super(manager, modDomain, entityStateFile, BABY);
	}

	@Override
	public RenderState getStateFromEntity(EntityChicken entity) {

		return getStateManager().getDefaultState()
			.withProperty(BABY, entity.isChild());
	}
}