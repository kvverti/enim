package kvverti.enim.entity.properties;

import java.util.Collection;

import net.minecraft.block.properties.PropertyHelper;
import net.minecraft.item.ItemArmor.ArmorMaterial;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

public class PropertyArmor extends PropertyHelper<ArmorMaterial> {

    private PropertyArmor(String name) {

        super(name, ArmorMaterial.class);
    }

    public static PropertyArmor create(String name) {

        return new PropertyArmor(name);
    }

    @Override
    public Collection<ArmorMaterial> getAllowedValues() {

        return ImmutableSet.copyOf(ArmorMaterial.values());
    }

    @Override
    public String getName(ArmorMaterial value) {

        return value.getName();
    }

    @Override
    public Optional<ArmorMaterial> parseValue(String name) {

        for(ArmorMaterial mat : ArmorMaterial.values())
            if(mat.getName().equals(name))
                return Optional.of(mat);
        return Optional.absent();
    }
}