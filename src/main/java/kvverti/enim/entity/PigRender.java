package kvverti.enim.entity;

import net.minecraft.block.properties.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityPig;

import kvverti.enim.entity.state.RenderState;

public class PigRender extends LivingRender<EntityPig> {

    public static final IProperty<Boolean> SADDLED = PropertyBool.create("saddled");

    public PigRender(RenderManager manager) {

        super(manager, BABY, SADDLED);
    }

    @Override
    public RenderState getStateFromEntity(EntityPig entity) {

        return getStateManager().getDefaultState()
            .withProperty(BABY, entity.isChild())
            .withProperty(SADDLED, entity.getSaddled());
    }
}