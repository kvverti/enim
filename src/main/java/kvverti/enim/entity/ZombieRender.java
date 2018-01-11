package kvverti.enim.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityZombie;

import kvverti.enim.entity.state.RenderState;

public class ZombieRender extends LivingRender<EntityZombie> {

    public ZombieRender(RenderManager manager) {

        super(manager, LivingRender.BABY);
    }

    @Override
    public RenderState getStateFromEntity(EntityZombie entity) {

        return getStateManager().getDefaultState()
            .withProperty(BABY, entity.isChild());
    }
}