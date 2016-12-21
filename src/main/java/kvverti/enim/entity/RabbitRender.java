package kvverti.enim.entity;

import java.util.Map;
import java.util.HashMap;

import net.minecraft.block.properties.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.util.EnumChatFormatting;

import kvverti.enim.entity.state.EnumStringSerializable;
import kvverti.enim.entity.state.RenderState;

public class RabbitRender extends LivingRender<EntityRabbit> {

	public static final IProperty<RabbitType> RABBIT_TYPE = PropertyEnum.create("type", RabbitType.class);

	public RabbitRender(RenderManager manager, String modDomain, String entityStateFile) {

		super(manager, modDomain, entityStateFile, RABBIT_TYPE, BABY);
	}

	@Override
	public RenderState getStateFromEntity(EntityRabbit entity) {

		String name = EnumChatFormatting.getTextWithoutFormattingCodes(entity.getName());
		RabbitType type = "Toast".equals(name) ? RabbitType.TOAST
			: "Eclipse".equals(name) ? RabbitType.ECLIPSE
			: RabbitType.fromNbt(entity.getRabbitType());
		return getStateManager().getDefaultState()
			.withProperty(RABBIT_TYPE, type)
			.withProperty(BABY, entity.isChild());
	}

	public enum RabbitType implements EnumStringSerializable {

		BROWN		(0),
		WHITE		(1),
		BLACK		(2),
		GOLD		(3),
		SALT		(4),
		SPLOTCHED	(5),
		CAERBANNOG	(99),
		TOAST		(-1),
		ECLIPSE		(-2);

		private static final Map<Integer, RabbitType> intToType = new HashMap<>();
		static {

			for(RabbitType type : values())
				intToType.put(type.meta, type);
		}

		private final int meta;

		private RabbitType(int i) {

			meta = i;
		}

		public int nbtValue() {

			return meta;
		}

		public static RabbitType fromNbt(int value) {

			return intToType.getOrDefault(value, BROWN);
		}
	}
}