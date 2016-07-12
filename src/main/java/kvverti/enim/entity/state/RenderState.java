package kvverti.enim.entity.state;

import java.util.Collection;
import java.util.Map;

import net.minecraft.block.properties.IProperty;

public interface RenderState {

	Collection<IProperty<?>> getPropertyNames();
	<T extends Comparable<T>> T getValue(IProperty<T> property);
	<T extends Comparable<T>> RenderState withProperty(IProperty<T> property, T value);
	<T extends Comparable<T>> RenderState cycleProperty(IProperty<T> property);
	Map<IProperty<?>, Comparable<?>> getProperties();
	String toString();
}