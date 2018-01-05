package kvverti.enim.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import kvverti.enim.Vec3f;

/**
 * Class representing the scale property present in many places.
 * Scale may be specified as a single float or as a list of three floats.
 */
class ScaleProperty {

	public static final ScaleProperty ONE = new ScaleProperty(Vec3f.IDENTITY);

	public final Vec3f value;

	public ScaleProperty(Vec3f v) { value = v; }

	public static class Deserializer implements JsonDeserializer<ScaleProperty> {

		@Override
		public ScaleProperty deserialize(JsonElement json, Type type, JsonDeserializationContext context) {

			Vec3f value;
			if(json.isJsonPrimitive()) {
				float f = json.getAsFloat();
				value = Vec3f.of(f, f, f);
			} else
				value = context.deserialize(json, Vec3f.class);
			if(value.equals(Vec3f.IDENTITY))
				return ScaleProperty.ONE;
			return new ScaleProperty(value);
		}
	}
}