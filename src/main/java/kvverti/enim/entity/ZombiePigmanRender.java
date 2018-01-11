package kvverti.enim.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityPigZombie;

import kvverti.enim.entity.state.RenderState;

public class ZombiePigmanRender extends LivingRender<EntityPigZombie> {

    public ZombiePigmanRender(RenderManager manager) {

        super(manager, LivingRender.BABY);
    }

    @Override
    public RenderState getStateFromEntity(EntityPigZombie entity) {

        return getStateManager().getDefaultState()
            .withProperty(BABY, entity.isChild());
    }
}