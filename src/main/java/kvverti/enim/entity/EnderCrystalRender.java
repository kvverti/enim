package kvverti.enim.entity;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityEnderCrystal;

import kvverti.enim.entity.state.RenderState;

public class EnderCrystalRender extends ENIMRender<EntityEnderCrystal> {

	public static final IProperty<Boolean> BASEPLATE = PropertyBool.create("baseplate");

	public EnderCrystalRender(RenderManager manager) {

		super(manager, BASEPLATE);
	}

	@Override
	public RenderState getStateFromEntity(EntityEnderCrystal entity) {

		return getStateManager().getDefaultState()
			.withProperty(BASEPLATE, entity.shouldShowBottom());
	}
}