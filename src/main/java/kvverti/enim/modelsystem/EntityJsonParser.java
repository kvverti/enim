package kvverti.enim.modelsystem;

import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import com.google.gson.*;

import kvverti.enim.Logger;

public final class EntityJsonParser {

	private static final ResourceLocation MISSING_LOCATION = new ResourceLocation("missingno");

	private final IResource file;
	JsonObject json = null;

	public EntityJsonParser(IResource rsc) {

		file = rsc;
	}

	public void parseModelLocations(Set<String> states, Map<String, ? super EntityState> locs) throws ParserException {

		try {
			initJson();
			JsonObject obj = json.getAsJsonObject(Keys.STATES_TAG);
			for(Map.Entry<String, JsonElement> key : obj.entrySet()) {

				if(states.contains(key.getKey())) {

					EntityState state = parseEntityState(
						key.getKey(), key.getValue().getAsJsonObject());
					locs.put(key.getKey(), state);
				}
			}

		} catch(JsonParseException e) {

			throw new ParserException(e);
		}
	}

	public EntityState parseEntityState(String name, JsonObject obj) throws ParserException {

		try {
			ResourceLocation model;
			ResourceLocation texture;
			float[] rotation = new float[3];
			float scale;
			int[] texSize;

			model = getResourceLocation(obj.get(Keys.STATE_MODEL_NAME).getAsString(), "models/entity/", ".json");
			texture = getResourceLocation(obj.get(Keys.STATE_TEXTURE).getAsString(), "textures/entity/", ".png");
			rotation[0] = getFloat(obj, Keys.STATE_ROTATION, 0);
			rotation[1] = getFloat(obj, Keys.STATE_ROTATION, 1);
			rotation[2] = getFloat(obj, Keys.STATE_ROTATION, 2);
			scale = getScaleOptional(obj, Keys.STATE_SCALE);
			texSize = getDims(obj);

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
			for(JsonElement elem : elems) {

				JsonObject obj = elem.getAsJsonObject();
				if((name == null ? "" : name).equals(getString(obj, Keys.ELEM_NAME))) {

					return buildElement(obj);
				}
			}
			return null;

		} catch(JsonParseException|SyntaxException e) {

			throw new ParserException(e);
		}
	}

	public void parseElements(Set<? super ModelElement> set) throws ParserException {

		try {
			initJson();
			if(!json.has(Keys.ELEMENTS_TAG)) return;
			JsonArray elems = json.getAsJsonArray(Keys.ELEMENTS_TAG);
			for(JsonElement elem : elems) {

				ModelElement mdelem = buildElement(elem.getAsJsonObject());
				addSafely(set, mdelem);
			}

		} catch(JsonParseException|SyntaxException e) {

			throw new ParserException(e);
		}
	}

	public void getElementImports(Set<? super ModelElement> set) throws ParserException {

		try {
			initJson();
			if(!json.has(Keys.IMPORTS_TAG)) return;
			JsonObject imports = json.getAsJsonObject(Keys.IMPORTS_TAG);
			for(Map.Entry<String, JsonElement> entry : imports.entrySet()) {

				EntityJsonParser parser = getParserFor(entry.getKey());
				JsonArray arr = entry.getValue().getAsJsonArray();
				if(contains(arr, new JsonPrimitive(Keys.WILDCARD))) {

					parser.parseElements(set);

				} else for(JsonElement elem : arr) {

					ModelElement mdl = parser.parseElement(elem.getAsString());
					if(mdl != null) addSafely(set, mdl);
					else throw new ElementNotFoundException(elem.getAsString());
				}
			}

		} catch(JsonParseException|IOException e) {

			throw new ParserException(e);
		}
	}

	private void addSafely(Set<? super ModelElement> set, ModelElement elem) throws DuplicateElementException {

		if(!set.add(elem)) throw new DuplicateElementException(
			elem.getName() + " in file " + file.getResourceLocation());
	}

	private ResourceLocation getResourceLocation(String loc, String relative, String ext) {

		ResourceLocation result;
		Matcher m = Keys.RESOURCE_LOCATION_REGEX.matcher(loc);
		if(loc != null && m.matches()) {

			result = new ResourceLocation(
				m.group("domain"), relative + m.group("filepath") + ext);

		} else result = MISSING_LOCATION;
		return result;
	}

	private EntityJsonParser getParserFor(String key) throws IOException {

		ResourceLocation loc = getResourceLocation(key, "models/entity/", ".json");
		IResource nextResource = Minecraft.getMinecraft().getResourceManager().getResource(loc);
		return new EntityJsonParser(nextResource);
	}

	private ModelElement buildElement(JsonObject obj) throws SyntaxException {

		return new ModelElement.Builder()
			.setName(getString(obj, Keys.ELEM_NAME))
			.setParent(getString(obj, Keys.ELEM_PARENT))
			.setTexCoords(getInt(obj, Keys.ELEM_TEXCOORDS, 0),
				getInt(obj, Keys.ELEM_TEXCOORDS, 1))
			.setFrom(getFloat(obj, Keys.ELEM_FROM, 0),
				getFloat(obj, Keys.ELEM_FROM, 1),
				getFloat(obj, Keys.ELEM_FROM, 2))
			.setTo(getFloat(obj, Keys.ELEM_TO, 0),
				getFloat(obj, Keys.ELEM_TO, 1),
				getFloat(obj, Keys.ELEM_TO, 2))
			.setRotationPoint(getFloat(obj, Keys.ELEM_ROTPOINT, 0),
				getFloat(obj, Keys.ELEM_ROTPOINT, 1),
				getFloat(obj, Keys.ELEM_ROTPOINT, 2))
			.setDefaultRotation(getFloat(obj, Keys.ELEM_DEFROT, 0),
				getFloat(obj, Keys.ELEM_DEFROT, 1),
				getFloat(obj, Keys.ELEM_DEFROT, 2))
			.setScale(getScaleOptional(obj, Keys.ELEM_SCALE))
			.build();
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
		return elem == null || elem.isJsonNull() || elem.getAsString().length() == 0 ? null : elem.getAsString();
	}

	private int getInt(JsonObject obj, String key, int index) {

		JsonArray arr = obj.getAsJsonArray(key);
		return arr == null || arr.isJsonNull() || index >= arr.size() ? 0 : arr.get(index).getAsInt();
	}

	private float getFloat(JsonObject obj, String key, int index) {

		JsonArray arr = obj.getAsJsonArray(key);
		return arr == null || arr.isJsonNull() || index >= arr.size() ? 0.0f : arr.get(index).getAsFloat();
	}

	private void initJson() throws ParserException {

		if(json != null) return;
		try(InputStream istream = file.getInputStream();
		BufferedReader breader = new BufferedReader(new InputStreamReader(istream))) {

			json = new Gson().fromJson(breader, JsonElement.class).getAsJsonObject();

		} catch(IOException e) {

			Logger.warn("IO-error occured closing file");

		} catch(JsonParseException e) {

			throw new ParserException(e);
		}
	}
}