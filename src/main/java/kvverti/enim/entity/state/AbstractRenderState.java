package kvverti.enim.entity.state;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.properties.IProperty;

import kvverti.enim.Keys;

import static java.util.stream.Collectors.joining;

public abstract class AbstractRenderState implements RenderState {

	protected AbstractRenderState() { }

	@Override
	public <T extends Comparable<T>> RenderState cycleProperty(IProperty<T> property) {

		return withProperty(property, cycle(property, getValue(property)));
	}

	private <T extends Comparable<T>> T cycle(IProperty<T> property, T current) {

		boolean flag = false;
		for(T value : property.getAllowedValues()) {

			if(flag) return value;
			else if(value.equals(current))
				flag = true;
		}
		//current was the last element, return the first
		return property.getAllowedValues().iterator().next();
	}

	@Override
	public String toString() {

		return getPropertyNames().isEmpty() ? Keys.STATE_NORMAL : getPropertyNames().stream()
			.map(prop -> prop.getName() + "=" + getNameHelper(prop))
			.sorted()
			.collect(joining(","));
	}

	private <T extends Comparable<T>> String getNameHelper(IProperty<T> property) {

		return property.getName(getValue(property));
	}
}