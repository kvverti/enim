package kvverti.enim.entity.state;

import net.minecraft.util.IStringSerializable;

public interface EnumStringSerializable extends IStringSerializable {

    /* Declared in Enum */
    String name();

    @Override
    default String getName() {

        return name().toLowerCase();
    }
}