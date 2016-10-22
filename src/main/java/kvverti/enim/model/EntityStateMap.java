package kvverti.enim.model;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.AbstractMap;

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ForwardingMap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import kvverti.enim.Keys;
import kvverti.enim.Util;

/** An immutable collection of EntityStates for use in deserializing entitystate files. */
public class EntityStateMap extends ForwardingMap<String, EntityState> {

	private final ImmutableMap<String, EntityState> states;

	/** For Json deserialization. */
	public EntityStateMap(Map<String, EntityState> states) { this.states = ImmutableMap.copyOf(states); }

	@Override
	protected Map<String, EntityState> delegate() { return states; }

	/** Deserializer for the {@link EntityStateMap} class. */
	public static class Deserializer implements JsonDeserializer<EntityStateMap> {

		private static final Type statesType = new TypeToken<Map<String, EntityState>>(){}.getType();

		@Override
		public EntityStateMap deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

			JsonObject obj = json.getAsJsonObject();
			EntityStateMap res = new EntityStateMap(context.deserialize(obj.get(Keys.STATES_TAG), statesType));
			EntityState.Defaults stateDefaults = obj.has(Keys.STATES_DEFAULTS) ?
				context.deserialize(obj.get(Keys.STATES_DEFAULTS), EntityState.Defaults.class)
				: new EntityState.Defaults();
			for(EntityState state : res.states.values())
				state.replaceDefaults(stateDefaults);
			return res;
		}
	}
}