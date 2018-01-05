package kvverti.enim.entity;

import net.minecraft.block.properties.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntitySlime;

import kvverti.enim.entity.state.RenderState;

public class SlimeRender extends LivingRender<EntitySlime> {

    public static final IProperty<Integer> SLIME_SIZE = PropertyInteger.create("size", 0, 3);

    public SlimeRender(RenderManager manager) {

        super(manager, SLIME_SIZE);
    }

    @Override
    public RenderState getStateFromEntity(EntitySlime entity) {

        int size = entity.getSlimeSize() - 1;
        if     (size > 3) size = 3;
        else if(size < 0) size = 0;
        return getStateManager().getDefaultState().withProperty(SLIME_SIZE, size);
    }

    @Override
    protected void preRender(EntitySlime entity, EntityInfo info) {

        super.preRender(entity, info);
        float squish = 2.0f / (2.0f + entity.squishFactor);
        GlStateManager.scale(squish, 1.0f / squish, squish);
    }
}