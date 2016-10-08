package kvverti.enim.model;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.AbstractMap;

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ForwardingMap;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
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
			Defaults stateDefaults = obj.has(Keys.STATES_DEFAULTS) ?
				context.deserialize(obj.get(Keys.STATES_DEFAULTS), Defaults.class)
				: new Defaults();
			for(EntityState state : res.states.values()) {

				if(state.texture == null)
					state.texture = Util.getResourceLocation(stateDefaults.texture, Keys.TEXTURES_DIR, Keys.PNG);
				if(state.size[0] == -1 && state.size[1] == -1) {

					state.size[0] = stateDefaults.size[0];
					state.size[1] = stateDefaults.size[1];
				}
				if(Float.isNaN(state.scale))
					state.scale = stateDefaults.scale;
				if(Float.isNaN(state.y))
					state.y = stateDefaults.y;
			}
			return res;
		}

		/** Class representing the "defaults" tag in an entitystate file */
		private static class Defaults {

			@SerializedName(Keys.STATE_TEXTURE)
			String texture = null;

			@SerializedName(Keys.STATE_TEX_SIZE)
			final int[] size = { 64, 32 };

			@SerializedName(Keys.STATE_SCALE)
			float scale = 1.0f;

			@SerializedName(Keys.STATE_ROTATION)
			float y = 0.0f;
		}
	}
}