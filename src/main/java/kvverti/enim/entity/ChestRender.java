package kvverti.enim.entity;

import java.util.Calendar;

import net.minecraft.block.BlockChest;
import net.minecraft.block.properties.*;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.entity.state.RenderState;

public class ChestRender extends ENIMTileEntityRender<TileEntityChest> {

	public static final IProperty<EnumFacing> FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final IProperty<Boolean> DOUBLE = PropertyBool.create("double");
	public static final IProperty<Boolean> OPEN = PropertyBool.create("open");
	public static final IProperty<Boolean> TRAPPED = PropertyBool.create("trapped");

	private static final ResourceLocation XMAS = new ResourceLocation("textures/entity/chest/christmas.png");
	private static final ResourceLocation XMAS_DOUBLE = new ResourceLocation("textures/entity/chest/christmas_double.png");

	private final boolean xmas;

	public ChestRender() {

		super(FACING, DOUBLE, OPEN, TRAPPED);
		Calendar c = Calendar.getInstance();
		if(c.get(Calendar.MONTH) == Calendar.DECEMBER) {

			int day = c.get(Calendar.DATE);
			xmas = day >= 24 && day <= 26;
		} else
			xmas = false;
	}

	public ChestRender(int unused) {

		super(FACING, DOUBLE, OPEN, TRAPPED);
		xmas = true;
	}

	{
		RenderState state = getStateManager().getDefaultState();
		getStateManager().setDefaultState(state
			.withProperty(TRAPPED, false)
			.withProperty(FACING, EnumFacing.SOUTH)
			.withProperty(DOUBLE, false)
			.withProperty(OPEN, false));
	}

	@Override
	public RenderState getStateFromTile(TileEntityChest tile) {

		RenderState state = getStateManager().getDefaultState()
			.withProperty(TRAPPED, tile.getChestType() == BlockChest.Type.TRAP);
		if(!tile.hasWorld())
			return state;
		return state
			.withProperty(DOUBLE, tile.adjacentChestZPos != null || tile.adjacentChestXPos != null
				|| tile.adjacentChestXNeg != null || tile.adjacentChestZNeg != null)
			.withProperty(FACING, EnumFacing.getFront(tile.getBlockMetadata()))
			.withProperty(OPEN, tile.lidAngle > 0.0f);
	}

	@Override
	public boolean shouldRender(TileEntityChest tile) {

		if(!tile.hasWorld())
			return true;
		EnumFacing facing = EnumFacing.getFront(tile.getBlockMetadata());
		boolean pos = tile.adjacentChestZPos != null || tile.adjacentChestXPos != null;
		boolean neg = tile.adjacentChestZNeg == null && tile.adjacentChestXNeg == null;
		return facing == EnumFacing.SOUTH || facing == EnumFacing.WEST ? pos || neg : !neg || !pos;
	}

	@Override
	public void preRender(TileEntityChest tile, EntityInfo info) {

		if(xmas) {

			boolean doubleChest = tile.adjacentChestZPos != null || tile.adjacentChestXPos != null
				|| tile.adjacentChestXNeg != null || tile.adjacentChestZNeg != null;
			bindTexture(doubleChest ? XMAS_DOUBLE : XMAS);
		}
	}
}