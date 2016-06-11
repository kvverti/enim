package kvverti.enim.entity;

import java.util.Set;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.modelsystem.EntityState;

public class SignLikeRender<T extends TileEntity> extends ENIMTileEntityRender<T> {

	private final Block floorBlock;

	public SignLikeRender(String modDomain, String entityStateFile, Block floor) {

		super(modDomain, entityStateFile,
			"wall_north",
			"wall_south",
			"wall_east",
			"wall_west",
			"floor_00",
			"floor_01",
			"floor_02",
			"floor_03",
			"floor_04",
			"floor_05",
			"floor_06",
			"floor_07",
			"floor_08",
			"floor_09",
			"floor_10",
			"floor_11",
			"floor_12",
			"floor_13",
			"floor_14",
			"floor_15");
		floorBlock = floor;
	}

	@Override
	public EntityState getStateFromTile(T tile) {

		Block block = tile.getBlockType();
		EntityState entityState = null;

		if(block == floorBlock) {

			switch(tile.getBlockMetadata()) {

				case 0: entityState = getState("floor_00");
					break;
				case 1: entityState = getState("floor_01");
					break;
				case 2: entityState = getState("floor_02");
					break;
				case 3: entityState = getState("floor_03");
					break;
				case 4: entityState = getState("floor_04");
					break;
				case 5: entityState = getState("floor_05");
					break;
				case 6: entityState = getState("floor_06");
					break;
				case 7: entityState = getState("floor_07");
					break;
				case 8: entityState = getState("floor_08");
					break;
				case 9: entityState = getState("floor_09");
					break;
				case 10: entityState = getState("floor_10");
					break;
				case 11: entityState = getState("floor_11");
					break;
				case 12: entityState = getState("floor_12");
					break;
				case 13: entityState = getState("floor_13");
					break;
				case 14: entityState = getState("floor_14");
					break;
				case 15: entityState = getState("floor_15");
					break;
			}

		} else {

			switch(tile.getBlockMetadata()) {

				case 3: entityState = getState("wall_south");
					break;
				case 2: entityState = getState("wall_north");
					break;
				case 4: entityState = getState("wall_east");
					break;
				case 5: entityState = getState("wall_west");
					break;
				default: entityState = getState("floor_00");
					break;
			}
		}
		return entityState;
	}
}