package kvverti.enim.model.multipart;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.Map;

import com.google.gson.*;

import kvverti.enim.entity.state.RenderState;

/** Represents a "when" tag in a multipart entitystate file. */
public abstract class Condition {

	public static final Condition TRUE = new Condition() {

		@Override
		public boolean fulfills(RenderState state) { return true; }
	};

	Condition() { }

	public abstract boolean fulfills(RenderState state);

	public static class Deserializer implements JsonDeserializer<Condition> {

		@Override
		public Condition deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

			//it's on OR
			if(json.isJsonArray())
				return ConditionOr.deserialize(json, context);
			Set<Map.Entry<String, JsonElement>> entries = json.getAsJsonObject().entrySet();
			//it's a single
			if(entries.size() == 1) {

				Map.Entry<String, JsonElement> entry = entries.iterator().next();
				return new ConditionSingle(entry.getKey(), entry.getValue().getAsString());
			}
			//it's an AND
			return ConditionAnd.deserialize(json, context);
		}
	}
}