package kvverti.enim.entity;

import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.EnumFacing;

import kvverti.enim.entity.state.RenderState;

import static kvverti.enim.entity.ChestRender.FACING;
import static kvverti.enim.entity.ChestRender.OPEN;

public class EnderChestRender extends ENIMTileEntityRender<TileEntityEnderChest> {

	public EnderChestRender() {

		super(FACING, OPEN);
		RenderState state = getStateManager().getDefaultState();
		getStateManager().setDefaultState(state
			.withProperty(FACING, EnumFacing.SOUTH)
			.withProperty(OPEN, false));
	}

	@Override
	public RenderState getStateFromTile(TileEntityEnderChest tile) {

		if(!tile.hasWorld())
			return getStateManager().getDefaultState();
		return getStateManager().getDefaultState()
			.withProperty(FACING, EnumFacing.getFront(tile.getBlockMetadata()))
			.withProperty(OPEN, tile.lidAngle > 0.0f);
	}
}