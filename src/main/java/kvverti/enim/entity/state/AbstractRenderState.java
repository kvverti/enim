package kvverti.enim.entity.state;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.properties.IProperty;

import kvverti.enim.modelsystem.Keys;

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

		if(getPropertyNames().isEmpty()) return Keys.STATE_NORMAL;

		List<IProperty<?>> alphabetized = new ArrayList<>(getPropertyNames());
		alphabetized.sort((left, right) -> left.getName().compareTo(right.getName()));
		StringBuilder sb = new StringBuilder();
		alphabetized.forEach(property ->
			sb.append(property.getName())
			.append('=')
			.append(getNameHelper(property))
			.append(','));
		sb.deleteCharAt(sb.length() - 1); //trailing comma
		return sb.toString();
	}

	private <T extends Comparable<T>> String getNameHelper(IProperty<T> property) {

		return property.getName(getValue(property));
	}
}