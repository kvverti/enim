package kvverti.enim.entity.state;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.block.properties.IProperty;

import com.google.common.collect.ImmutableSet;

import kvverti.enim.entity.ENIMModel;
import kvverti.enim.model.EntityModel;
import kvverti.enim.model.EntityStateMap;
import kvverti.enim.model.EntityState;
import kvverti.enim.Keys;
import kvverti.enim.Logger;

public class StateManager {

	private final Map<String, EntityState> stateMap = new HashMap<>();
	private final Map<String, ENIMModel> modelMap = new HashMap<>();
	private final Map<IProperty<?>, Comparable<?>> defaults = new HashMap<>();
	private final ImmutableSet<String> stateNames;
	private final Collection<IProperty<?>> properties;

	public StateManager(IProperty<?>... properties) {

		this.properties = Arrays.asList(properties.clone());
		fillRecursive(properties);
		this.stateNames = ImmutableSet.copyOf(stateMap.keySet());
	}

	private final void fillRecursive(IProperty<?>[] properties) {

		if(properties.length == 0) {

			modelMap.put(Keys.STATE_NORMAL, new ENIMModel());
			stateMap.put(Keys.STATE_NORMAL, EntityModel.MISSING_STATE);
		}
		else fillRecursiveImpl(new MutableRenderState(properties), properties, 0);
	}

	private final void fillRecursiveImpl(RenderState state, IProperty<?>[] properties, int index) {

		if(index >= properties.length) return;
		int length = properties[index].getAllowedValues().size();
		for(int i = 0; i < length; i++) {

			fillRecursiveImpl(state, properties, index + 1);
			String str = state.cycleProperty(properties[index]).toString();
			modelMap.put(str, new ENIMModel());
			stateMap.put(str, null);
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

	public ImmutableSet<String> stateStringNames() {

		return stateNames;
	}

	public void reloadStates(Map<String, EntityState> states) {

		if(!stateMap.keySet().containsAll(states.keySet()))
			throw new IllegalArgumentException("Invalid states " + states.keySet() + "for properties " + properties);
		stateMap.replaceAll((k, v) -> states.get(k));
		modelMap.forEach((str, model) -> {

			EntityState state = stateMap.get(str);
			model.reload(state.model(), state.size()[0], state.size()[1]);
		});
	}

	public EntityState getState(RenderState state) {

		EntityState es = stateMap.get(state.toString());
		if(es == null)
			throw new IllegalArgumentException("State " + state + " is invalid for properties: " + properties);
		return es;
	}

	public ENIMModel getModel(RenderState state) {

		ENIMModel model = modelMap.get(state.toString());
		if(model == null)
			throw new IllegalArgumentException("State " + state + " is invalid for properties: " + properties);
		return model;
	}

	public void setAllInvalid() {

		modelMap.values().forEach(ENIMModel::setMissingno);
		stateMap.replaceAll((k, v) -> EntityModel.MISSING_STATE);
	}
}