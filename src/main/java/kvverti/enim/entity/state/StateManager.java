package kvverti.enim.entity.state;

//import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;

import kvverti.enim.entity.ENIMModel;
import kvverti.enim.model.EntityModel;
import kvverti.enim.model.EntityState;
import kvverti.enim.Keys;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class StateManager {

	private final Map<String, EntityState> stateMap = new HashMap<>();
	private final Map<String, ENIMModel> modelMap = new HashMap<>();
	private final ImmutableSet<String> stateNames;
	private final Collection<IProperty<?>> properties;
	private final BlockState stateDelegate;
	private final Map<IBlockState, RenderState> renderStates;
	private RenderState defaultState;

	@SuppressWarnings("unchecked")
	public StateManager(IProperty<?>... properties) {

		this.stateDelegate = new BlockState(Blocks.air, properties);
		this.properties = (Collection) stateDelegate.getProperties();
		this.renderStates = stateDelegate.getValidStates().stream()
			.collect(toMap(Function.identity(), BlockForwardingRenderState::new));
		this.defaultState = renderStates.get(stateDelegate.getBaseState());
		fillRecursive(properties);
		this.stateNames = ImmutableSet.copyOf(stateMap.keySet());
	}

	private final void fillRecursive(IProperty<?>[] properties) {

		if(properties.length == 0) {

			modelMap.put(Keys.STATE_NORMAL, new ENIMModel());
			stateMap.put(Keys.STATE_NORMAL, EntityModel.MISSING_STATE);
		}
		else fillRecursiveImpl(defaultState, properties, 0);
	}

	private final void fillRecursiveImpl(RenderState state, IProperty<?>[] properties, int index) {

		if(index >= properties.length) return;
		int length = properties[index].getAllowedValues().size();
		for(int i = 0; i < length; i++) {

			fillRecursiveImpl(state, properties, index + 1);
			state = state.cycleProperty(properties[index]);
			String str = state.toString();
			modelMap.put(str, new ENIMModel());
			stateMap.put(str, EntityModel.MISSING_STATE);
		}
	}

	public RenderState getDefaultState() {

		return defaultState;
	}

	public void setDefaultState(RenderState state) {

		Collection<IProperty<?>> cp = state.getPropertyNames();
		if(!properties.containsAll(cp) || !cp.containsAll(properties))
			throw new IllegalArgumentException("Cannot set default state, properties mismatch");
		defaultState = state;
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

	private class BlockForwardingRenderState implements RenderState {

		private final IBlockState delegate;

		public BlockForwardingRenderState(IBlockState state) {

			delegate = state;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Collection<IProperty<?>> getPropertyNames() {

			return (Collection) delegate.getPropertyNames();
		}

		@Override
		@SuppressWarnings("unchecked")
		public ImmutableMap<IProperty<?>, Comparable<?>> getProperties() {

			return (ImmutableMap) delegate.getProperties();
		}

		@Override
		public <T extends Comparable<T>> T getValue(IProperty<T> property) {

			return delegate.getValue(property);
		}

		@Override
		public <T extends Comparable<T>> RenderState withProperty(IProperty<T> property, T value) {

			return renderStates.get(delegate.withProperty(property, value));
		}

		@Override
		public <T extends Comparable<T>> RenderState cycleProperty(IProperty<T> property) {

			return renderStates.get(delegate.cycleProperty(property));
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
}