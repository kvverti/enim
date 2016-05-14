package kvverti.enim.entity;

import java.util.Set;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.modelsystem.EntityState;

public abstract class SignLikeRender<T extends TileEntity> extends ENIMTileEntityRender<T> {

	private final Block floorBlock;

	public SignLikeRender(String modDomain, String entityStateFile, Block floor) {

		super(modDomain, entityStateFile);
		floorBlock = floor;
	}

	@Override
	public Set<String> getEntityStateNames() {

		Set<String> s = new HashSet<>();
		s.add("wall_north");
		s.add("wall_south");
		s.add("wall_east");
		s.add("wall_west");
		s.add("floor_00");
		s.add("floor_01");
		s.add("floor_02");
		s.add("floor_03");
		s.add("floor_04");
		s.add("floor_05");
		s.add("floor_06");
		s.add("floor_07");
		s.add("floor_08");
		s.add("floor_09");
		s.add("floor_10");
		s.add("floor_11");
		s.add("floor_12");
		s.add("floor_13");
		s.add("floor_14");
		s.add("floor_15");
		return s;
	}

	@Override
	public EntityState getStateFromTile(T tile) {

		Block block = tile.getBlockType();
		EntityState entityState = null;

		if(block == floorBlock) {

			switch(tile.getBlockMetadata()) {

				case 0: entityState = states.get("floor_00");
					break;
				case 1: entityState = states.get("floor_01");
					break;
				case 2: entityState = states.get("floor_02");
					break;
				case 3: entityState = states.get("floor_03");
					break;
				case 4: entityState = states.get("floor_04");
					break;
				case 5: entityState = states.get("floor_05");
					break;
				case 6: entityState = states.get("floor_06");
					break;
				case 7: entityState = states.get("floor_07");
					break;
				case 8: entityState = states.get("floor_08");
					break;
				case 9: entityState = states.get("floor_09");
					break;
				case 10: entityState = states.get("floor_10");
					break;
				case 11: entityState = states.get("floor_11");
					break;
				case 12: entityState = states.get("floor_12");
					break;
				case 13: entityState = states.get("floor_13");
					break;
				case 14: entityState = states.get("floor_14");
					break;
				case 15: entityState = states.get("floor_15");
					break;
			}

		} else {

			switch(tile.getBlockMetadata()) {

				case 3: entityState = states.get("wall_south");
					break;
				case 2: entityState = states.get("wall_north");
					break;
				case 4: entityState = states.get("wall_east");
					break;
				case 5: entityState = states.get("wall_west");
					break;
				default: entityState = states.get("floor_00");
					break;
			}
		}
		return entityState;
	}
}