package kvverti.enim.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityZombieVillager;

import kvverti.enim.entity.state.RenderState;
import kvverti.enim.entity.VillagerRender.Profession;

import static kvverti.enim.entity.VillagerRender.PROFESSION;

public class ZombieVillagerRender extends LivingRender<EntityZombieVillager> {
    
    public ZombieVillagerRender(RenderManager manager) {
        
        super(manager, BABY, PROFESSION);
    }
    
    @Override
    public RenderState getStateFromEntity(EntityZombieVillager entity) {
        
        return getStateManager().getDefaultState()
            .withProperty(BABY, entity.isChild())
            .withProperty(PROFESSION, Profession.byName(
                entity.getForgeProfession().getRegistryName().getResourcePath()));
    }
}