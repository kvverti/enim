package kvverti.enim.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityPigZombie;

import kvverti.enim.entity.state.RenderState;

public class ZombiePigmanRender extends ArmorLivingRender<EntityPigZombie> {

    public ZombiePigmanRender(RenderManager manager) {

        super(manager, LivingRender.BABY);
    }

    @Override
    public RenderState getStateFromEntity(EntityPigZombie entity) {

        return super.getStateFromEntity(entity)
            .withProperty(BABY, entity.isChild());
    }
}