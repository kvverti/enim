package kvverti.enim.entity;

import java.util.Set;
import java.util.HashSet;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.util.EnumChatFormatting;

import kvverti.enim.modelsystem.EntityState;

public class RabbitRender extends LivingRender<EntityRabbit> {

	public RabbitRender(RenderManager manager, String modDomain, String entityStateFile) {

		super(manager, modDomain, entityStateFile);
	}

	@Override
	public Set<String> getEntityStateNames() {

		Set<String> set = new HashSet<>();
		set.add("brown");
		set.add("white");
		set.add("black");
		set.add("gold");
		set.add("salt");
		set.add("splotched");
		set.add("caerbannog");
		set.add("toast");
		set.add("eclipse");
		set.add("baby_brown");
		set.add("baby_white");
		set.add("baby_black");
		set.add("baby_gold");
		set.add("baby_salt");
		set.add("baby_splotched");
		set.add("baby_caerbannog");
		set.add("baby_toast");
		set.add("baby_eclipse");
		return set;
	}

	@Override
	public EntityState getStateFromEntity(EntityRabbit entity) {

		String baby = entity.isChild() ? "baby_" : "";
		String name = EnumChatFormatting.getTextWithoutFormattingCodes(entity.getName());
		if("Toast".equals(name))
			return states.get(baby + "toast");
		else if("Eclipse".equals(name))
			return states.get(baby + "eclipse");
		else switch(entity.getRabbitType()) {

			case 0: return states.get(baby + "brown");
			case 1: return states.get(baby + "white");
			case 2: return states.get(baby + "black");
			case 3: return states.get(baby + "splotched");
			case 4: return states.get(baby + "gold");
			case 5: return states.get(baby + "salt");
			case 99: return states.get(baby + "caerbannog");
			default: return states.get(baby + "brown");
		}
	}
}