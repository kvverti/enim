package kvverti.enim.modelsystem;

import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import com.google.gson.*;

import kvverti.enim.Logger;
import kvverti.enim.Vec3f;
import kvverti.enim.entity.Entities;
import kvverti.enim.Util;
import kvverti.enim.Util.*;

public final class EntityJsonParser {

	private static final JsonObject EMPTY_JSON_OBJ = new JsonObject();
	private static final Gson gson = new GsonBuilder()
		.registerTypeAdapter(Vec3f.class, new Vec3f.Adapter().nullSafe())
		.create();

	private final IResource file;
	JsonObject json = null;

	public EntityJsonParser(IResource rsc) {

		file = rsc;
	}

	public void parseModelLocations(Set<String> states, Map<String, ? super EntityState> locs) throws ParserException {

		try {
			initJson();
			JsonObject obj = json.getAsJsonObject(Keys.STATES_TAG);
			JsonObject defaults = json.has(Keys.STATES_DEFAULTS) ?
				json.getAsJsonObject(Keys.STATES_DEFAULTS)
				: EMPTY_JSON_OBJ;
			for(Map.Entry<String, JsonElement> key : obj.entrySet()) {

				if(states.contains(key.getKey())) {

					EntityState state = parseEntityState(
						key.getKey(), key.getValue().getAsJsonObject(), defaults);
					locs.put(key.getKey(), state);
				}
			}

		} catch(JsonParseException e) {

			throw new ParserException(e);
		}
	}

	private EntityState parseEntityState(String name, JsonObject obj, JsonObject defaults) throws ParserException {

		try {
			ResourceLocation model = null;
			ResourceLocation texture = null;
			float rotation;
			float scale;
			int[] texSize;

			//fill default properties
			if(defaults.has(Keys.STATE_TEXTURE))
				texture = getResourceLocation(
					defaults.get(Keys.STATE_TEXTURE).getAsString(), Keys.TEXTURES_DIR, Keys.PNG);
			rotation = getFloat(defaults, Keys.STATE_ROTATION);
			scale = getScaleOptional(defaults, Keys.STATE_SCALE);
			texSize = getDims(defaults);

			//fill specific properties
			if(obj.has(Keys.STATE_MODEL_NAME))
				model = getResourceLocation(
					obj.get(Keys.STATE_MODEL_NAME).getAsString(), Keys.MODELS_DIR, Keys.JSON);
			if(obj.has(Keys.STATE_TEXTURE))
				texture = getResourceLocation(
					obj.get(Keys.STATE_TEXTURE).getAsString(), Keys.TEXTURES_DIR, Keys.PNG);
			if(obj.has(Keys.STATE_ROTATION))
				rotation = getFloat(obj, Keys.STATE_ROTATION);
			if(obj.has(Keys.STATE_SCALE))
				scale = getScaleOptional(obj, Keys.STATE_SCALE);
			if(obj.has(Keys.STATE_TEX_SIZE))
				texSize = getDims(obj);

			if(model == null || texture == null) throw new ParserException("Model or texture not found");
			return new EntityState(name, model, rotation, scale, texture, texSize[0], texSize[1]);

		} catch(JsonParseException|SyntaxException e) {

			throw new ParserException(e);
		}
	}

	private int[] getDims(JsonObject object) throws SyntaxException {

		int[] result = { getInt(object, Keys.STATE_TEX_SIZE, 0), getInt(object, Keys.STATE_TEX_SIZE, 1) };
		if(result[0] < 0 || result[1] < 0) throw new SyntaxException(
			"Negative texture size in " + file.getResourceLocation());
		return result;
	}

	public ModelElement parseElement(String name) throws ParserException {

		try {
			initJson();
			if(!json.has(Keys.ELEMENTS_TAG)) return null;
			JsonArray elems = json.getAsJsonArray(Keys.ELEMENTS_TAG);
			return StreamSupport.stream(elems.spliterator(), false)
				.map(JsonElement::getAsJsonObject)
				.filter(obj -> getString(obj, Keys.ELEM_NAME).equals(name))
				.map(ThrowingFunction.of(this::buildElement))
				.findFirst()
				.orElseThrow(() -> new ElementNotFoundException(name));

		} catch(JsonParseException e) {

			throw new ParserException(e);

		} catch(WrappedCheckedException e) {

			throw new ParserException(e.getCause());
		}
	}

	public void parseElements(Set<? super ModelElement> set) throws ParserException {

		try {
			initJson();
			if(!json.has(Keys.ELEMENTS_TAG)) return;
			JsonArray elems = json.getAsJsonArray(Keys.ELEMENTS_TAG);
			StreamSupport.stream(elems.spliterator(), false)
				.map(JsonElement::getAsJsonObject)
				.map(ThrowingFunction.of(this::buildElement))
				.forEach(ThrowingConsumer.of(modelElem -> addSafely(set, modelElem)));

		} catch(JsonParseException e) {

			throw new ParserException(e);

		} catch(WrappedCheckedException e) {

			throw e.ifInstance(ParserException.class)
				.orElseWrap(ParserException::new);
		}
	}

	public void applyOverrides(Set<ModelElement> set) throws ParserException {

		try {
			initJson();
			if(!json.has(Keys.OVERRIDES_TAG)) return;
			JsonObject overrides = json.getAsJsonObject(Keys.OVERRIDES_TAG);
			set.stream()
				.filter(elem -> overrides.has(elem.name))
				.forEach(elem -> replaceOverride(overrides, elem));

		} catch(JsonParseException e) {

			throw new ParserException(e);
		}
	}

	private ModelElement replaceOverride(JsonObject overrides, ModelElement elem) {

		JsonObject obj = overrides.getAsJsonObject(elem.name);
		if(obj.has(Keys.ELEM_SCALE))
			elem.scale = getScaleOptional(obj, Keys.ELEM_SCALE);
		if(obj.has(Keys.ELEM_DEFROT))
			elem.rotation = getVec3f(obj, Keys.ELEM_DEFROT);
		return elem;
	}

	public void getImports(Set<? super ModelElement> set, Map<AnimationType, Animation> map) throws ParserException {

		try {
			initJson();
			if(!json.has(Keys.IMPORTS_TAG)) return;
			JsonObject imports = json.getAsJsonObject(Keys.IMPORTS_TAG);
			for(Map.Entry<String, JsonElement> entry : imports.entrySet()) {

				EntityJsonParser parser = getParserFor(entry.getKey());
				for(JsonElement elem : entry.getValue().getAsJsonArray()) {

					String str = elem.getAsString();
					if(str.startsWith(Keys.ANIMS_TAG + ":")) {

						str = str.substring(Keys.ANIMS_TAG.length() + 1);
						if(Keys.WILDCARD.equals(str))
							parser.parseAnimations(map);
						else {
							Animation a = parser.getAnimation(str);
							if(a != Animation.NO_OP) map.put(AnimationType.from(str), a);
						}
					} else {

						if(Keys.WILDCARD.equals(str))
							parser.parseElements(set);
						else addSafely(set, parser.parseElement(str));
					}
				}
			}

		} catch(JsonParseException|IOException e) {

			throw new ParserException(e);
		}
	}

	public void parseAnimations(Map<? super AnimationType, ? super Animation> map) throws ParserException {

		initJson();
		if(json.has(Keys.ANIMS_TAG)) {

			try {
				for(AnimationType type : AnimationType.values()) {

					Animation a = getAnimation(type.key());
					if(a != Animation.NO_OP) map.put(type, a);
				}

			} catch(JsonParseException|IOException e) {

				throw new ParserException(e);
			}

		} else for(AnimationType type : AnimationType.values()) {

			map.putIfAbsent(type, Animation.NO_OP);
		}
	}

	private Animation getAnimation(String str) throws IOException, ParserException {

		initJson();
		if(json.has(Keys.ANIMS_TAG)) {

			JsonObject anim = json.getAsJsonObject(Keys.ANIMS_TAG).getAsJsonObject(str);
			if(anim == null) return Animation.NO_OP;
			ResourceLocation loc = getResourceLocation(
				anim.get(Keys.ANIM_SCRIPT).getAsString(), Keys.ANIMS_DIR, Keys.ENIM);
			IResource animFile = Entities.resourceManager().getResource(loc);
			JsonObject defines = anim.getAsJsonObject(Keys.ANIM_DEFINES);
			Map<String, String> defineMap = new HashMap<>();
			defines.entrySet().forEach(entry -> defineMap.put(entry.getKey(), getString(defines, entry.getKey())));
			return Animation.compile(animFile, defineMap);

		} else return Animation.NO_OP;
	}

	private void addSafely(Set<? super ModelElement> set, ModelElement elem) throws DuplicateElementException {

		if(!set.add(elem))
			throw new DuplicateElementException(elem.name + " in file " + file.getResourceLocation());
	}

	private ResourceLocation getResourceLocation(String loc, String relative, String ext) {

		Matcher m = Keys.RESOURCE_LOCATION_REGEX.matcher(loc);
		return loc != null && m.matches() ?
			new ResourceLocation(m.group("domain"), relative + m.group("filepath") + ext)
			: Util.MISSING_LOCATION;
	}

	private EntityJsonParser getParserFor(String key) throws IOException {

		ResourceLocation loc = getResourceLocation(key, Keys.MODELS_DIR, Keys.JSON);
		IResource nextResource = Entities.resourceManager().getResource(loc);
		return new EntityJsonParser(nextResource);
	}

	private ModelElement buildElement(JsonObject obj) throws SyntaxException {

		/*ModelElement element = new ModelElement(getString(obj, Keys.ELEM_NAME));
		element.parent = getString(obj, Keys.ELEM_PARENT);
		element.uv[0] = getInt(obj, Keys.ELEM_TEXCOORDS, 0);
		element.uv[1] = getInt(obj, Keys.ELEM_TEXCOORDS, 1);
		element.from = getVec3f(obj, Keys.ELEM_FROM);
		element.to = getVec3f(obj, Keys.ELEM_TO);
		element.origin = getVec3f(obj, Keys.ELEM_ROTPOINT);
		element.rotation = getVec3f(obj, Keys.ELEM_DEFROT);
		element.scale = getScaleOptional(obj, Keys.ELEM_SCALE);
		element.translucent = getBoolean(obj, Keys.ELEM_TRANSLUCENT);
		element.head = getBoolean(obj, Keys.ELEM_HEAD);*/
		ModelElement element = gson.fromJson(obj, ModelElement.class);
		element.verify();
		return element;
	}

	private Vec3f getVec3f(JsonObject obj, String key) {

		float x = getFloat(obj, key, 0);
		float y = getFloat(obj, key, 1);
		float z = getFloat(obj, key, 2);
		return Vec3f.of(x, y, z);
	}

	private boolean getBoolean(JsonObject obj, String key) {

		JsonElement elem = obj.get(key);
		return elem == null || elem.isJsonNull() ? false : elem.getAsBoolean();
	}

	private float getScaleOptional(JsonObject obj, String key) {

		JsonElement p = obj.get(key);
		return p == null || p.isJsonNull() ? 1.0f : p.getAsFloat();
	}

	private boolean contains(JsonArray arr, JsonElement elem) {

		for(JsonElement e : arr) {

			if(e.equals(elem)) return true;
		}
		return false;
	}

	private String getString(JsonObject obj, String key) {

		JsonElement elem = obj.get(key);
		return elem == null || elem.isJsonNull() || elem.getAsString().length() == 0 ? "" : elem.getAsString();
	}

	private int getInt(JsonObject obj, String key, int index) {

		JsonArray arr = obj.getAsJsonArray(key);
		return arr == null || arr.isJsonNull() || index >= arr.size() ? 0 : arr.get(index).getAsInt();
	}

	private float getFloat(JsonObject obj, String key) {

		JsonElement elem = obj.get(key);
		return elem == null || elem.isJsonNull() ? 0.0f : elem.getAsFloat();
	}

	private float getFloat(JsonObject obj, String key, int index) {

		JsonArray arr = obj.getAsJsonArray(key);
		return arr == null || arr.isJsonNull() || index >= arr.size() ? 0.0f : arr.get(index).getAsFloat();
	}

	private void initJson() throws ParserException {

		if(json != null) return;
		try(InputStream istream = file.getInputStream();
		BufferedReader breader = new BufferedReader(new InputStreamReader(istream))) {

			json = gson.fromJson(breader, JsonElement.class).getAsJsonObject();

		} catch(IOException e) {

			Logger.warn("IO-error occured closing file");

		} catch(JsonParseException e) {

			throw new ParserException(e);
		}
	}
}