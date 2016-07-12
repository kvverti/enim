package kvverti.enim.entity.state;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.block.properties.IProperty;

import kvverti.enim.modelsystem.EntityState;
import kvverti.enim.modelsystem.Keys;
import kvverti.enim.Logger;

public class StateManager {

	private final Map<String, EntityState> nameToModel = new HashMap<>();
	private final Map<IProperty<?>, Comparable<?>> defaults = new HashMap<>();
	private final Collection<IProperty<?>> properties;

	public StateManager(IProperty<?>... properties) {

		this.properties = Arrays.asList(properties.clone());
		fillRecursive(properties);
		Logger.info(nameToModel.keySet().toString());
	}

	private final void fillRecursive(IProperty<?>[] properties) {

		if(properties.length == 0)
			nameToModel.put(Keys.STATE_NORMAL, new EntityState(Keys.STATE_NORMAL));
		else fillRecursiveImpl(new MutableRenderState(properties), properties, 0);
	}

	private int __fillCounter = 0;

	private final void fillRecursiveImpl(RenderState state, IProperty<?>[] properties, int index) {

		Logger.info("Iteration: " + __fillCounter++);
		if(index >= properties.length) return;
		int length = properties[index].getAllowedValues().size();
		for(int i = 0; i < length; i++) {

			fillRecursiveImpl(state, properties, index + 1);
			String str = state.cycleProperty(properties[index]).toString();
			nameToModel.put(str, new EntityState(str));
		}
	}

	public RenderState getDefaultState() {

		RenderState res = new MutableRenderState(properties);
		defaults.forEach((property, value) -> defaultStateHelper(res, property, value));
		return res;
	}

	private <T extends Comparable<T>>
		void defaultStateHelper(RenderState state, IProperty<T> property, Comparable<?> value) {

		state.withProperty(property, property.getValueClass().cast(value));
	}

	public void setDefaultState(RenderState state) {

		Collection<IProperty<?>> cp = state.getPropertyNames();
		if(!properties.containsAll(cp) || !cp.containsAll(properties))
			throw new IllegalArgumentException("Cannot set default state, properties mismatch");
		defaults.clear();
		defaults.putAll(state.getProperties());
	}

	public Set<String> stateStringNames() {

		return new HashSet<>(nameToModel.keySet());
	}

	public void setState(EntityState features) {

		EntityState es = nameToModel.get(features.name());
		if(es == null)
			throw new IllegalArgumentException("State " + features.name() + " cannot be set for properties: " + properties);
		es.reloadState(features);
	}

	public EntityState getState(RenderState state) {

		EntityState es = nameToModel.get(state.toString());
		if(es == null)
			throw new IllegalArgumentException("State " + state + " is invalid for properties: " + properties);
		return es;
	}

	public void setAllInvalid() {

		nameToModel.values().forEach(state -> state.model().setMissingno());
	}
}