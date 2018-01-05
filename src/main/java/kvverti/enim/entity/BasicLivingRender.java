package kvverti.enim.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;

import kvverti.enim.entity.state.RenderState;

public class BasicLivingRender<T extends EntityLivingBase> extends LivingRender<T> {

    public BasicLivingRender(RenderManager manager) {

        super(manager);
    }

    @Override
    public RenderState getStateFromEntity(T entity) {

        return getStateManager().getDefaultState();
    }
}