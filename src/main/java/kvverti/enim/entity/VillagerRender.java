package kvverti.enim.entity;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityVillager;

import kvverti.enim.entity.state.EnumStringSerializable;
import kvverti.enim.entity.state.RenderState;

public class VillagerRender extends LivingRender<EntityVillager> {

    public static final IProperty<Profession> PROFESSION = PropertyEnum.create("profession", Profession.class);

    public VillagerRender(RenderManager manager) {

        super(manager, BABY, PROFESSION);
    }

    @Override
    public RenderState getStateFromEntity(EntityVillager entity) {

        return getStateManager().getDefaultState()
            .withProperty(BABY, entity.isChild())
            .withProperty(PROFESSION, Profession.byName(entity.getProfessionForge().getRegistryName().getResourcePath()));
    }

    public enum Profession implements EnumStringSerializable {

        FARMER,
        LIBRARIAN,
        PRIEST,
        SMITH,
        BUTCHER,
        NITWIT;

        //so we aren't creating an array 20+ times a second
        private static final Profession[] values = Profession.values();

        public static Profession byName(String name) {

            for(Profession p : values)
                if(p.getName().equals(name))
                    return p;
            return FARMER;
        }
    }
}