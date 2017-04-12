package kvverti.enim.model.multipart;

import java.lang.reflect.Type;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;

import kvverti.enim.model.EntityState;

/** Model layer(s) to apply under a certain condition */
public class Rule {

	private final Condition condition;
	private final ImmutableList<EntityState> layers;

	private Rule(Condition c, ImmutableList<EntityState> l) { condition = c; layers = l; }

	public Condition condition() { return condition; }

	public ImmutableList<EntityState> layers() { return layers; }

	public static class Deserializer implements JsonDeserializer<Rule> {

		@Override
		public Rule deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

			JsonObject obj = json.getAsJsonObject();
			Condition c = obj.has("when") ? context.deserialize(obj.get("when"), Condition.class) : Condition.TRUE;
			JsonElement apply = obj.get("apply");
			ImmutableList<EntityState> layers = apply.isJsonArray() ?
				ImmutableList.copyOf(context.<EntityState[]>deserialize(apply, EntityState[].class))
				: ImmutableList.of(context.deserialize(apply, EntityState.class));
			if(layers.isEmpty())
				throw new JsonParseException("Layers list must be non-empty");
			return new Rule(c, layers);
		}
	}
}