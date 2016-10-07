package kvverti.enim.model;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import kvverti.enim.Keys;
import kvverti.enim.Vec3f;

/** Class corresponding to entity Json models */
public class EntityModel {

	/**
	 * The Gson instance used to deserialize Enim models and AbieScript animations. Note that many of these classes are deserialized lossily,
	 * they cannot be serialized into Json again.
	 */
	public static final Gson GSON = new GsonBuilder()
		.registerTypeAdapter(Vec3f.class, new Vec3f.Adapter().nullSafe())
		.registerTypeAdapter(ModelImports.class, new ModelImports.Deserializer())
		.registerTypeAdapter(Animation.class, new Animation.Deserializer())
		.registerTypeAdapter(EntityState.class, new EntityState.Deserializer())
		.registerTypeAdapter(EntityStateMap.class, new EntityStateMap.Deserializer())
		.registerTypeAdapter(EntityModel.class, new EntityModel.Deserializer())
		.create();

	public final ModelProperties properties;
	public final ImmutableSet<ModelElement> elements;
	public final ImmutableMap<Animation.Type, Animation> animations;

	/** For Json deserialization */
	private EntityModel(ModelProperties properties, Set<ModelElement> elements, Map<Animation.Type, Animation> animations) {

		this.properties = properties;
		this.elements = ImmutableSet.copyOf(elements);
		this.animations = ImmutableMap.copyOf(animations);
	}

	@Override
	public String toString() {

		return String.format("EntityModel { \"%s\": %s, \"%s\": %s, \"%s\": %s }",
			Keys.PROPERTIES_TAG, properties,
			Keys.ELEMENTS_TAG, elements,
			Keys.ANIMS_TAG, animations);
	}

	/** Deserializer for the {@link EntityModel} class. */
	public static class Deserializer implements JsonDeserializer<EntityModel> {

		private static final Type elementsType = new TypeToken<Set<ModelElement>>(){}.getType();
		private static final Type animsType = new TypeToken<Map<Animation.Type, Animation>>(){}.getType();
		private static final Type overridesType = new TypeToken<Map<String, ModelElement.Override>>(){}.getType();

		@Override
		public EntityModel deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

			JsonObject obj = json.getAsJsonObject();
			//essential fields of the EntityModel class
			ModelProperties properties = obj.has(Keys.PROPERTIES_TAG) ?
				context.deserialize(obj.get(Keys.PROPERTIES_TAG), ModelProperties.class)
				: ModelProperties.DEFAULT;
			Set<ModelElement> elements = obj.has(Keys.ELEMENTS_TAG) ?
				context.deserialize(obj.get(Keys.ELEMENTS_TAG), elementsType)
				: new HashSet<>();
			Map<Animation.Type, Animation> animations = obj.has(Keys.ANIMS_TAG) ?
				context.deserialize(obj.get(Keys.ANIMS_TAG), animsType)
				: new HashMap<>();

			//other json objects that do not have an in-code representation
			if(obj.has(Keys.IMPORTS_TAG)) {

				ModelImports imports = context.deserialize(obj.get(Keys.IMPORTS_TAG), ModelImports.class);
				elements.addAll(imports.elements);
				for(Map.Entry<Animation.Type, Animation> entry : imports.animations.entrySet())
					animations.putIfAbsent(entry.getKey(), entry.getValue());
			}
			if(obj.has(Keys.ELEMENTS_OVERRIDES)) {

				Map<String, ModelElement.Override> overrides =
					context.deserialize(obj.get(Keys.ELEMENTS_OVERRIDES), overridesType);
				for(ModelElement element : elements)
					element.applyOverride(overrides.get(element.name));
			}
			return new EntityModel(properties, elements, animations);
		}
	}
}