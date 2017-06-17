package kvverti.enim.entity;

import java.util.Objects;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A GEntity (general entity) wraps either an entity or a tile entity. This enables Enim systems to treat entities
 * and tile entities as common objects.
 */
public final class GEntity {

	private final Object value; //nonnull
	private final boolean tile;

	public GEntity(Entity entity) {

		value = checkNotNull(entity);
		tile = false;
	}

	public GEntity(TileEntity tile) {

		value = checkNotNull(tile);
		this.tile = true;
	}

	public boolean isTileEntity() {

		return tile;
	}

	public Object getValue() {

		return value;
	}

	public Entity getEntity() {

		return (Entity) value;
	}

	public TileEntity getTileEntity() {

		return (TileEntity) value;
	}

	public Class<?> getEntityClass() {

		return value.getClass();
	}

	public Class<?> getRootClass() {

		return tile ? TileEntity.class : Entity.class;
	}

	public int getCounterSeed() {

		return Objects.hash(tile ? getTileEntity().getPos() : getEntity().getUniqueID());
	}

	@Override
	public boolean equals(Object obj) {

		if(!(obj instanceof GEntity))
			return false;
		return value == ((GEntity) obj).value;
	}

	@Override
	public int hashCode() {

		return value.hashCode();
	}
}