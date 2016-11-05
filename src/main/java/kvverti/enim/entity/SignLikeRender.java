package kvverti.enim.entity;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.properties.*;
import net.minecraft.tileentity.TileEntity;

import kvverti.enim.entity.state.RenderState;
import kvverti.enim.entity.state.EnumStringSerializable;

public class SignLikeRender<T extends TileEntity> extends ENIMTileEntityRender<T> {

	public static final IProperty<SignFacing> ROTATION = PropertyEnum.create("rotation", SignFacing.class);

	private final Block floorBlock;

	public SignLikeRender(String modDomain, String entityStateFile, Block floor) {

		super(modDomain, entityStateFile, ROTATION);
		floorBlock = floor;
	}

	@Override
	public RenderState getStateFromTile(T tile) {

		int meta = tile.getBlockMetadata();
		SignFacing facing = tile.getBlockType() == floorBlock ?
			SignFacing.fromFloor(meta)
			: SignFacing.fromWall(meta);
		return getStateManager().getDefaultState().withProperty(ROTATION, facing);
	}

	public enum SignFacing implements EnumStringSerializable {

		N	(0),
		NNE	(1),
		NE	(2),
		ENE	(3),
		E	(4),
		ESE	(5),
		SE	(6),
		SSE	(7),
		S	(8),
		SSW	(9),
		SW	(10),
		WSW	(11),
		W	(12),
		WNW	(13),
		NW	(14),
		NNW	(15),
		NORTH	(2),
		SOUTH	(3),
		EAST	(4),
		WEST	(5);

		private static final Map<Integer, SignFacing> wallToFacing = new HashMap<>();
		private static final Map<Integer, SignFacing> floorToFacing = new HashMap<>();
		static {

			wall().forEach(value -> wallToFacing.put(value.meta, value));
			floor().forEach(value -> floorToFacing.put(value.meta, value));
		}

		private final int meta;

		private SignFacing(int i) {

			meta = i;
		}

		public int nbtValue() {

			return meta;
		}

		public static Set<SignFacing> wall() {

			return EnumSet.range(NORTH, WEST);
		}

		public static Set<SignFacing> floor() {

			return EnumSet.range(N, NNW);
		}

		public static SignFacing fromFloor(int rotation) {

			return floorToFacing.getOrDefault(rotation, N);
		}

		public static SignFacing fromWall(int facing) {

			return wallToFacing.getOrDefault(facing, NORTH);
		}
	}
}