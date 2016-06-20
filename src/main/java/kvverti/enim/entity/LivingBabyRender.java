package kvverti.enim.entity;

import java.util.Set;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;

import kvverti.enim.modelsystem.EntityState;

public abstract class LivingBabyRender<T extends EntityLivingBase> extends LivingRender<T> {

	protected LivingBabyRender(RenderManager manager, String modDomain, String entityStateFile, String... stateNames) {

		super(manager, modDomain, entityStateFile, addBabies(stateNames));
	}

	public abstract String getAdultStateFromEntity(T entity);

	@Override
	public final EntityState getStateFromEntity(T entity) {

		return getState((entity.isChild() ? "baby_" : "") + getAdultStateFromEntity(entity));
	}

	private static String[] addBabies(String[] adultStates) {

		String[] res = new String[adultStates.length * 2];
		for(int i = 0; i < adultStates.length; i++) {

			res[i] = "baby_" + adultStates[i];
		}
		System.arraycopy(adultStates, 0, res, adultStates.length, adultStates.length);
		return res;
	}
}