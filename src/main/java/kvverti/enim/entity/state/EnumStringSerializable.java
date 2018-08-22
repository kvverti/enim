package kvverti.enim.entity.state;

import net.minecraft.util.IStringSerializable;

public interface EnumStringSerializable extends IStringSerializable {

    @Override
    default String getName() {

        return ((Enum<?>) this).name().toLowerCase();
    }
}
