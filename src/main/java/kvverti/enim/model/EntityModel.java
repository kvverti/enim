package kvverti.enim.model;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
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

	/**
	 * The invalid ("missingno") entity model.
	 */
	public static final EntityModel MISSING_MODEL = GSON.fromJson(
		"{\"properties\":{\"nameplate\":18},\"elements\":[{\"name\":\"missingno\",\"from\":[0,0,0],\"to\":[16,16,16],\"uv\":[0,0]}]}",
		EntityModel.class);

	/**
	 * The invalid ("missingno") entity state.
	 */
	public static final EntityState MISSING_STATE = GSON.fromJson(
		"{\"model\":\"builtin/missingno\",\"texture\":\"builtin/missingno\",\"size\":[64,32],\"y\":0,\"scale\":1}",
		EntityState.class);

	private final ModelProperties properties;
	private final ImmutableSet<ModelElement> elements;
	private final ImmutableMap<String, ModelElement> elementMap;
	private final ImmutableMap<Animation.Type, Animation> animations;

	/** For Json deserialization */
	private EntityModel(ModelProperties properties, Set<ModelElement> elements, Map<Animation.Type, Animation> animations) {

		this.properties = properties;
		this.elements = ImmutableSet.copyOf(elements);
		this.animations = ImmutableMap.copyOf(animations);
		ImmutableMap.Builder<String, ModelElement> b = new ImmutableMap.Builder<>();
		for(ModelElement m : elements)
			b.put(m.name(), m);
		elementMap = b.build();
	}

	/**
	 * This model's properties. The model properties contain miscellaneous data about the model, such as nameplate height and held item
	 * positions.
	 * @return this model's properties
	 * @see ModelProperties
	 */
	public ModelProperties properties() { return properties; }

	/**
	 * This model's elements. The model elements are cuboid and contain data such as size, position, and texture coordinates. To get a
	 * specific element, the {@code getElement(String)} method should be used instead.
	 * @return an immutable set containing this model's elements
	 * @see ModelElement
	 * @see #getElement(String)
	 */
	public ImmutableSet<ModelElement> elements() { return elements; }

	/**
	 * This model's animations. The model animations control the transformations of the elements under certain conditions, such as movement
	 * or interaction. If there is no animation for a given animation type, is will not be present in the returned map.
	 * @return an immutable map containing this model's animations
	 * @see Animation
	 */
	public ImmutableMap<Animation.Type, Animation> animations() { return animations; }

	/**
	 * Returns the element in this model with the given name. The name must match exactly with an element in this model. To get all
	 * elements, use {@code elements()}.
	 * @param name the name of the element to return
	 * @return the element with the given name
	 * @throws NoSuchElementException if this model does not contain an element with the given name
	 */
	public ModelElement getElement(String name) {

		if(elementMap.containsKey(name))
			return elementMap.get(name);
		throw new NoSuchElementException(name);
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
					element.applyOverride(overrides.get(element.name()));
			}
			return new EntityModel(properties, elements, animations);
		}
	}
}