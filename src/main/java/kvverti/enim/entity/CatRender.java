package kvverti.enim.entity;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityOcelot;

import kvverti.enim.entity.state.EnumStringSerializable;
import kvverti.enim.entity.state.RenderState;

public class CatRender extends LivingRender<EntityOcelot> {

    public static final IProperty<CatType> CAT_TYPE = PropertyEnum.create("type", CatType.class);
    public static final IProperty<Boolean> SITTING = PropertyBool.create("sitting");

    public CatRender(RenderManager manager) {

        super(manager, CAT_TYPE, SITTING, LivingRender.BABY);
    }

    @Override
    public RenderState getStateFromEntity(EntityOcelot entity) {

        return getStateManager().getDefaultState()
            .withProperty(CAT_TYPE, CatType.fromSkin(entity.getTameSkin()))
            .withProperty(SITTING, entity.isSitting())
            .withProperty(LivingRender.BABY, entity.isChild());
    }

    public enum CatType implements EnumStringSerializable {

        OCELOT,
        BLACK,
        RED,
        SIAMESE;

        /** Static cat type array */
        private static final CatType[] values = CatType.values();

        public static CatType fromSkin(int skin) {
            
            return values[skin];
        }
    }
}
