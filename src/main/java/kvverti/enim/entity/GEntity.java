package kvverti.enim.entity;

/** A GEntity (general entity) wraps either an entity or a tile entity. */
public final class GEntity {

	private final Object value;
	private final boolean tile;

	public GEntity(Entity entity) {

		value = entity;
		tile = false;
	}

	public GEntity(TileEntity tile) {

		value = entity;
		tile = true;
	}

	public boolean isTileEntity() {

		return tile;
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
}