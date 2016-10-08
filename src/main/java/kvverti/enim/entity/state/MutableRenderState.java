package kvverti.enim.entity.state;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.block.properties.IProperty;

import com.google.common.collect.ImmutableSet;

public class MutableRenderState extends AbstractRenderState {

	private final Map<IProperty<?>, Comparable<?>> properties = new HashMap<>();
	private final ImmutableSet<IProperty<?>> propertyNames;

	public MutableRenderState(IProperty<?>... propertyNames) {

		for(IProperty<?> property : propertyNames) {

			properties.put(property, property.getAllowedValues().iterator().next());
		}
		this.propertyNames = ImmutableSet.copyOf(properties.keySet());
	}

	public MutableRenderState(Collection<IProperty<?>> propertyNames) {

		for(IProperty<?> property : propertyNames) {

			properties.put(property, property.getAllowedValues().iterator().next());
		}
		this.propertyNames = ImmutableSet.copyOf(properties.keySet());
	}

	@Override
	public Collection<IProperty<?>> getPropertyNames() {

		return propertyNames;
	}

	@Override
	public Map<IProperty<?>, Comparable<?>> getProperties() {

		return Collections.unmodifiableMap(new HashMap<>(properties));
	}

	@Override
	public <T extends Comparable<T>> T getValue(IProperty<T> property) {

		if(!properties.containsKey(property))
			throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist!");
		return property.getValueClass().cast(properties.get(property));
	}

	@Override
	public <T extends Comparable<T>> RenderState withProperty(IProperty<T> property, T value) {

		if(!properties.containsKey(property))
			throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist!");
		if(!property.getAllowedValues().contains(value))
			throw new IllegalArgumentException("Value must be one of " + property.getAllowedValues() + "; got " + value);
		properties.put(property, value);
		return this;
	}
}