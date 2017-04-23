package kvverti.enim.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;

import kvverti.enim.entity.state.RenderState;

public class GrowableAnimalRender<T extends EntityLivingBase> extends LivingRender<T> {

	public GrowableAnimalRender(RenderManager manager) {

		super(manager, BABY);
	}

	@Override
	public RenderState getStateFromEntity(T entity) {

		return getStateManager().getDefaultState()
			.withProperty(BABY, entity.isChild());
	}
}