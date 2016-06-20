package kvverti.enim.entity;

import java.util.Set;

import net.minecraft.util.ResourceLocation;

import kvverti.enim.modelsystem.EntityState;

public interface ReloadableRender {

	ResourceLocation getEntityStateFile();
	Set<String> getEntityStateNames();
	void reloadRender(EntityState state);
	void setMissingno();
}