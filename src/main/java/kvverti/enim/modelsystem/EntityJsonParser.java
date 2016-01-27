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

	public void parseModelLocations(Map<String, ? super ResourceLocation> locs) throws ParserException {

		try {
			initJson();
			JsonObject obj = json.getAsJsonObject(Keys.STATES_TAG);
			for(Map.Entry<String, JsonElement> key : obj.entrySet()) {

				JsonObject mdl = key.getValue().getAsJsonObject();
				ResourceLocation loc = getResourceLocation(
					getString(mdl, Keys.STATE_MODEL_NAME), "models/entity/", ".json");
				locs.put(key.getKey(), loc);
			}

		} catch(JsonParseException e) {

			throw new ParserException(e);
		}
	}

	public void parseTextures(List<? super Texture> list) throws ParserException {

		try {
			initJson();
			if(!json.has(Keys.TEXTURE_TAG)) return;
			JsonObject obj = json.getAsJsonObject(Keys.TEXTURE_TAG);
			int[] dims = getDims(obj);
			JsonArray atlases = obj.getAsJsonArray(Keys.TEX_ATLASES);
			for(JsonElement elem : atlases) {

				ResourceLocation loc = getResourceLocation(elem.getAsString(), "textures/entity/", ".png");
				list.add(new Texture(loc, dims[0], dims[1]));
			}

		} catch(JsonParseException|SyntaxException e) {

			throw new ParserException(e);
		}
	}

	private int[] getDims(JsonObject object) throws SyntaxException {

		int[] result = { getInt(object, Keys.TEX_SIZE, 0), getInt(object, Keys.TEX_SIZE, 1) };
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
			.setFrom(getInt(obj, Keys.ELEM_FROM, 0),
				getInt(obj, Keys.ELEM_FROM, 1),
				getInt(obj, Keys.ELEM_FROM, 2))
			.setTo(getInt(obj, Keys.ELEM_TO, 0),
				getInt(obj, Keys.ELEM_TO, 1),
				getInt(obj, Keys.ELEM_TO, 2))
			.setRotationPoint(getInt(obj, Keys.ELEM_ROTPOINT, 0),
				getInt(obj, Keys.ELEM_ROTPOINT, 1),
				getInt(obj, Keys.ELEM_ROTPOINT, 2))
			.build();
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