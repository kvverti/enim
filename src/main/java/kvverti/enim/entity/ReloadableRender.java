package kvverti.enim.entity;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import net.minecraft.util.ResourceLocation;

import kvverti.enim.modelsystem.EntityState;

public interface ReloadableRender {

	List<ReloadableRender> renders = new ArrayList<>();

	ResourceLocation getEntityStateFile();
	Set<String> getEntityStateNames();
	void reloadRender(EntityState state);
	void setMissingno();
}