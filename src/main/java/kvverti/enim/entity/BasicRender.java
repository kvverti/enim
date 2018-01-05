package kvverti.enim.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

import kvverti.enim.entity.state.RenderState;

public class BasicRender<T extends Entity> extends ENIMRender<T> {

    public BasicRender(RenderManager manager) {

        super(manager);
    }

    @Override
    public RenderState getStateFromEntity(T entity) {

        return getStateManager().getDefaultState();
    }
}