package kvverti.enim.model;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Type;

import net.minecraft.util.ResourceLocation;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import kvverti.enim.Keys;
import kvverti.enim.Util;

/** Class corresponding to the {@value Keys#IMPORTS_TAG} tag of an entity model */
class ModelImports {

	public final Set<ModelElement> elements;
	public final Map<Animation.Type, Animation> animations;

	/** For Json deserialization */
	public ModelImports() { elements = new HashSet<>(); animations = new HashMap<>(); }

	@Override
	public String toString() {

		return String.format("ModelImports { \"elements\": %s, \"animations\": %s }", elements, animations);
	}

	/** Deserializer for the {@link ModelImports} class */
	public static class Deserializer implements JsonDeserializer<ModelImports> {

		private static final Type elementsType = new TypeToken<Set<ModelElement>>(){}.getType();
		private static final Type animationsType = new TypeToken<Map<Animation.Type, Animation>>(){}.getType();

		@Override
		public ModelImports deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

			ModelImports res = new ModelImports();
			for(Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {

				ResourceLocation location = Util.getResourceLocation(entry.getKey(), Keys.MODELS_DIR, Keys.JSON);
				validate(location != Util.MISSING_LOCATION, "Expected a ResourceLocation: " + entry.getKey());
				try(Reader reader = Util.getReaderFor(location)) {

					JsonObject importObj = EntityModel.GSON.fromJson(reader, JsonObject.class);
					JsonArray elements = importObj.has(Keys.ELEMENTS_TAG) ?
						importObj.getAsJsonArray(Keys.ELEMENTS_TAG) : new JsonArray();
					JsonObject animations = importObj.has(Keys.ANIMS_TAG) ?
						importObj.getAsJsonObject(Keys.ANIMS_TAG) : new JsonObject();
					String[] imports = context.deserialize(entry.getValue(), String[].class);
					for(String str : imports) {

						boolean isAnimation = str.startsWith(Keys.ANIMS_TAG + ":");
						if(isAnimation) str = str.substring((Keys.ANIMS_TAG + ":").length());
						if(str.equals(Keys.WILDCARD))
							if(isAnimation) res.animations.putAll(context.deserialize(animations, animationsType));
							else res.elements.addAll(context.deserialize(elements, elementsType));
						else	if(isAnimation) res.animations.put(
								context.deserialize(new JsonPrimitive(str), Animation.Type.class),
								context.deserialize(animations.get(str), Animation.class));
							else res.elements.add(context.deserialize(getElement(str, elements), ModelElement.class));
					}
				} catch(IOException e) { throw new JsonIOException("IO-error parsing imports", e); }
			}
			return res;
		}

		private JsonElement getElement(String name, JsonArray elements) {

			for(JsonElement elem : elements)
				if(elem.getAsJsonObject().get(Keys.ELEM_NAME).getAsString().equals(name))
					return elem;
			throw new JsonParseException("Parsing imports - element not found: " + name);
		}

		private void validate(boolean condition, Object message) throws JsonParseException {

			if(!condition) throw new JsonParseException(String.valueOf(message));
		}
	}
}