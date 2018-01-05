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

    /** Creates a GEntity wrapping the given nonnull entity. */
    public GEntity(Entity entity) {

        value = checkNotNull(entity);
        tile = false;
    }

    /** Creates a GEntity wrapping the given nonnull tile entity. */
    public GEntity(TileEntity tile) {

        value = checkNotNull(tile);
        this.tile = true;
    }

    /** Returns whether this object wraps a tile entity. If false, this object wraps an entity. */
    public boolean isTileEntity() {

        return tile;
    }

    /** Returns the wrapped value. */
    public Object getValue() {

        return value;
    }

    /** Returns the wrapped value as an entity. */
    public Entity getEntity() {

        return (Entity) value;
    }

    /** Returns the wrapped value as a tile entity. */
    public TileEntity getTileEntity() {

        return (TileEntity) value;
    }

    /** Returns the class of the wrapped object. */
    public Class<?> getEntityClass() {

        return value.getClass();
    }

    /**
     * Returns {@code Entity.class} if this object wraps an entity, or {@code TileEntity.class}
     * if this object wraps a tile entity.
     */
    public Class<?> getRootClass() {

        return tile ? TileEntity.class : Entity.class;
    }

    /** Returns a number to be used as a seed for this entity or tile entity. The number is consistent across calls. */
    public int getCounterSeed() {

        return Objects.hash(tile ? getTileEntity().getPos() : getEntity().getUniqueID());
    }

    /**
     * Returns whether the given object is equal to this GEntity. An object is equal to this if and only if
     * the object is also a GEntity and their wrapped values are equal.
     */
    @Override
    public boolean equals(Object obj) {

        if(!(obj instanceof GEntity))
            return false;
        return value == ((GEntity) obj).value;
    }

    /** The hash code of a GEntity is the hash code of its wrapped value. */
    @Override
    public int hashCode() {

        return value.hashCode();
    }
}