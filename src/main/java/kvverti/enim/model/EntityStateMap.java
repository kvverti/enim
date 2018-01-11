package kvverti.enim.model;

import java.lang.reflect.Type;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ForwardingMap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import kvverti.enim.Keys;
import kvverti.enim.entity.state.RenderState;
import kvverti.enim.model.multipart.Rule;

/** An immutable collection of EntityStates for use in deserializing entitystate files. */
public class EntityStateMap extends ForwardingMap<String, ImmutableList<EntityState>> {

    private static final EntityState EMPTY = EntityModel.GSON.fromJson(
        "{\"model\":\"builtin/missingno\",\"texture\":\"builtin/missingno\",\"size\":[64,32],\"y\":0,\"scale\":0}",
        EntityState.class);

    private final ImmutableMap<String, ImmutableList<EntityState>> states;

    /** For Json deserialization. */
    public EntityStateMap(Map<String, ImmutableList<EntityState>> states) { this.states = ImmutableMap.copyOf(states); }

    @Override
    protected Map<String, ImmutableList<EntityState>> delegate() { return states; }

    public static EntityStateMap from(Reader json, List<RenderState> states) {

        //convert to json
        JsonObject obj = EntityModel.GSON.fromJson(json, JsonObject.class);
        //deal with multipart
        if(obj.has(Keys.MULTIPART_TAG)) {

            Rule[] rules = EntityModel.GSON.fromJson(obj.get(Keys.MULTIPART_TAG), Rule[].class);
            Map<String, ImmutableList<EntityState>> stateMap = new HashMap<>();
            for(RenderState renderState : states) {
                ImmutableList<EntityState> ls = getEntityState(renderState, rules);
                replaceDefaults(obj, ls);
                stateMap.put(renderState.toString(), ls);
            }
            return new EntityStateMap(stateMap);
        //it's the normal states
        } else
            return EntityModel.GSON.fromJson(obj, EntityStateMap.class);
    }

    private static ImmutableList<EntityState> getEntityState(RenderState renderState, Rule[] rules) {

        List<EntityState> layers = new ArrayList<>();
        for(Rule r : rules)
            if(r.condition().fulfills(renderState))
                layers.addAll(r.layers());
        if(layers.isEmpty())
            return ImmutableList.of(EMPTY);
        return ImmutableList.copyOf(layers);
    }

    private static void replaceDefaults(JsonObject obj, Collection<EntityState> states) {

        EntityState.Defaults stateDefaults = obj.has(Keys.STATES_DEFAULTS) ?
            EntityModel.GSON.fromJson(obj.get(Keys.STATES_DEFAULTS), EntityState.Defaults.class)
            : new EntityState.Defaults();
        for(EntityState state : states)
            state.replaceDefaults(stateDefaults);
    }

    /** Deserializer for the {@link EntityStateMap} class. */
    public static class Deserializer implements JsonDeserializer<EntityStateMap> {

        private static final Type statesType = new TypeToken<Map<String, ImmutableList<EntityState>>>(){}.getType();

        @Override
        public EntityStateMap deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

            JsonObject obj = json.getAsJsonObject();
            EntityStateMap res = new EntityStateMap(context.deserialize(obj.get(Keys.STATES_TAG), statesType));
            for(ImmutableList<EntityState> ls : res.states.values())
                replaceDefaults(obj, ls);
            return res;
        }
    }
}