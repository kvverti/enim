package kvverti.enim.entity.state;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import kvverti.enim.entity.ENIMModel;
import kvverti.enim.model.EntityModel;
import kvverti.enim.model.EntityState;
import kvverti.enim.model.EntityStateMap;
import kvverti.enim.Keys;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class StateManager {

    private final Map<String, ImmutableList<EntityState>> stateMap = new HashMap<>();
    private final Map<EntityModel, ENIMModel> model2model = new HashMap<>();
    private final Collection<IProperty<?>> properties;
    private final BlockStateContainer stateDelegate;
    private final Map<IBlockState, RenderState> renderStates;
    private RenderState defaultState;

    public StateManager(IProperty<?>... properties) {

        this.stateDelegate = new BlockStateContainer(Blocks.AIR, properties);
        this.properties = stateDelegate.getProperties();
        this.renderStates = stateDelegate.getValidStates().stream()
            .collect(toMap(Function.identity(), BlockForwardingRenderState::new));
        this.defaultState = renderStates.get(stateDelegate.getBaseState());
        fillRecursive(properties);
    }

    private final void fillRecursive(IProperty<?>[] properties) {

        if(properties.length == 0)
            stateMap.put(Keys.STATE_NORMAL, ImmutableList.of(EntityModel.MISSING_STATE));
        else
            fillRecursiveImpl(defaultState, properties, 0);
        model2model.put(EntityModel.MISSING_MODEL, new ENIMModel());
    }

    private final void fillRecursiveImpl(RenderState state, IProperty<?>[] properties, int index) {

        if(index >= properties.length) return;
        int length = properties[index].getAllowedValues().size();
        for(int i = 0; i < length; i++) {

            fillRecursiveImpl(state, properties, index + 1);
            state = state.cycleProperty(properties[index]);
            String str = state.toString();
            stateMap.put(str, ImmutableList.of(EntityModel.MISSING_STATE));
        }
    }

    public RenderState getDefaultState() {

        return defaultState;
    }

    public ImmutableList<RenderState> getRenderStates() {

        return ImmutableList.copyOf(renderStates.values());
    }

    public void setDefaultState(RenderState state) {

        Collection<IProperty<?>> cp = state.getPropertyNames();
        if(!properties.containsAll(cp) || !cp.containsAll(properties))
            throw new IllegalArgumentException("Cannot set default state, properties mismatch");
        defaultState = state;
    }

    public void reloadStates(EntityStateMap states) {

        if(!stateMap.keySet().containsAll(states.keySet()))
            throw new IllegalArgumentException("Invalid states " + states.keySet() + "for properties " + properties);
        stateMap.replaceAll((k, v) -> states.get(k));
        Iterator<ENIMModel> old = new ArrayList<>(model2model.values()).iterator();
        model2model.clear();
        for(ImmutableList<EntityState> entry : stateMap.values()) {
            for(EntityState state : entry) {

                ENIMModel model = old.hasNext() ? old.next() : new ENIMModel();
                int[] s = state.size();
                model.reload(state.model(), s[0], s[1]);
                model2model.put(state.model(), model);
            }
        }
    }

    public ImmutableList<EntityState> getStateLayers(RenderState state) {

        String key = state.toString();
        if(stateMap.containsKey(key))
            return stateMap.get(key);
        throw new IllegalArgumentException("Invalid states " + key + " for properties " + properties);
    }

    public ENIMModel getModel(EntityState state) {

        ENIMModel res = model2model.get(state.model());
        if(res == null) {
            res = new ENIMModel();
            int[] s = state.size();
            res.reload(state.model(), s[0], s[1]);
            model2model.put(state.model(), res);
        }
        return res;
    }

    public void setAllInvalid() {

        stateMap.replaceAll((k, v) -> ImmutableList.of(EntityModel.MISSING_STATE));
    }

    private class BlockForwardingRenderState implements RenderState {

        private final IBlockState delegate;

        public BlockForwardingRenderState(IBlockState state) {

            delegate = state;
        }

        @Override
        public Collection<IProperty<?>> getPropertyNames() {

            return delegate.getPropertyKeys();
        }

        @Override
        public ImmutableMap<IProperty<?>, Comparable<?>> getProperties() {

            return delegate.getProperties();
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
