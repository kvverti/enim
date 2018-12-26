package kvverti.enim.entity;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import kvverti.enim.model.EntityState;

public class SnowmanRender extends BasicLivingRender<EntitySnowman> {

    private final ItemStack pumpkinHead = new ItemStack(Blocks.PUMPKIN);

    public SnowmanRender(RenderManager manager) {

        super(manager);
    }

    @Override
    protected void postRender(EntitySnowman entity, EntityInfo info) {

        if(entity.isPumpkinEquipped()) {

            EntityState state = getCurrentEntityState();
            float scale = state.scale();
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);
            renderItem(entity,
                info,
                pumpkinHead,
                TransformType.HEAD,
                state.model().properties().helmet());
            GlStateManager.popMatrix();
        }
        super.postRender(entity, info);
    }
}
