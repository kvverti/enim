package kvverti.enim.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;

import kvverti.enim.modelsystem.Keys;

public class BasicLivingBabyRender<T extends EntityLivingBase> extends LivingBabyRender<T> {

	private final String stateName;

	public BasicLivingBabyRender(RenderManager manager, String modDomain, String entityStateFile) {

		super(manager, modDomain, entityStateFile, entityStateFile);
		stateName = entityStateFile;
	}

	@Override
	public String getAdultStateFromEntity(T entity) {

		return stateName;
	}
}