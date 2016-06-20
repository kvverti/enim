package kvverti.enim.entity;

import java.util.Set;
import java.util.HashSet;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.util.EnumChatFormatting;

public class RabbitRender extends LivingBabyRender<EntityRabbit> {

	public RabbitRender(RenderManager manager, String modDomain, String entityStateFile) {

		super(manager, modDomain, entityStateFile,
			"brown",
			"white",
			"black",
			"gold",
			"salt",
			"splotched",
			"caerbannog",
			"toast",
			"eclipse");
	}

	@Override
	public String getAdultStateFromEntity(EntityRabbit entity) {

		String name = EnumChatFormatting.getTextWithoutFormattingCodes(entity.getName());
		if("Toast".equals(name))
			return "toast";
		else if("Eclipse".equals(name))
			return "eclipse";
		else switch(entity.getRabbitType()) {

			case 0: return "brown";
			case 1: return "white";
			case 2: return "black";
			case 3: return "splotched";
			case 4: return "gold";
			case 5: return "salt";
			case 99: return "caerbannog";
			default: return "brown";
		}
	}
}