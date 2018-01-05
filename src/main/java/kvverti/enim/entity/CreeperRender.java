package kvverti.enim.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityCreeper;

import kvverti.enim.entity.state.RenderState;

public class CreeperRender extends LivingRender<EntityCreeper> {

    public CreeperRender(RenderManager manager) {

        super(manager);
    }

    @Override
    public RenderState getStateFromEntity(EntityCreeper entity) {

        return getStateManager().getDefaultState();
    }
}