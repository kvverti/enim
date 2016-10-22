package kvverti.enim.model;

import java.lang.reflect.Type;
import java.io.InputStream;
import java.io.Reader;
import java.io.IOException;
import javax.imageio.ImageIO;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

import kvverti.enim.Keys;
import kvverti.enim.Logger;
import kvverti.enim.Util;
import kvverti.enim.entity.Entities;

/** An entry in the {@value Keys#STATES_TAG} tag of an entitystate file. */
public class EntityState {

	//optional elements will be filled in by the defaults if left with these "invalid" values
	private final EntityModel model;		//will NOT be filled, error if not specified
	private ResourceLocation texture = null;	//invalid value (null) will be filled with default
	private final int[] size = { -1, -1 };		//invalid value (-1) will be filled with default
	private float scale = Float.NaN;		//invalid value (NaN) will be filled with default
	private float y = Float.NaN;			//invalid value (NaN) will be filled with default

	/** For Json deserialization */
	private EntityState(EntityModel model) { this.model = model; }

	/**
	 * The model used by this state.
	 */
	public EntityModel model() { return model; }

	/**
	 * The texture used by this state. The returned ResourceLocation is a dynamic location, so it will not reflect the file path of the
	 * actual texture image.
	 */
	public ResourceLocation texture() { return texture; }

	/**
	 * The texture dimensions for this state. The returned array is not tied to this state.
	 */
	public int[] size() { return size.clone(); }

	/**
	 * The rendering scale for this state.
	 */
	public float scale() { return scale; }

	/**
	 * The rotation about the vertical axis this state should render at.
	 */
	public float y() { return y; }

	/** Binds a texture dynamically. This is needed because the texture may be reloaded many times over the course of a game session. */
	private static ResourceLocation bindTexture(ResourceLocation loc) {

		try(InputStream istream = Entities.resourceManager().getResource(loc).getInputStream()) {

			ResourceLocation tex = Entities.textureManager().getDynamicTextureLocation(
				"enim_entity_texture", new DynamicTexture(ImageIO.read(istream)));
			Entities.textureManager().bindTexture(tex);
			return tex;

		} catch(IOException e) {

			Logger.error("Could not bind texture for " + loc);
			return loc;
		}
	}

	/* For use by EntityStateMap */
	void replaceDefaults(Defaults defaults) {

		if(texture == null)
			texture = Util.getResourceLocation(defaults.texture, Keys.TEXTURES_DIR, Keys.PNG);
		if(size[0] == -1 && size[1] == -1) {

			size[0] = defaults.size[0];
			size[1] = defaults.size[1];
		}
		if(Float.isNaN(scale))
			scale = defaults.scale;
		if(Float.isNaN(y))
			y = defaults.y;
	}

	@Override
	public String toString() {

		return String.format("EntityState { \"%s\", %s, \"%s\": %s, \"%s\": [%d, %d], \"%s\": %f, \"%s\": %f }",
			Keys.STATE_MODEL_NAME, "[elided]",
			Keys.STATE_TEXTURE, texture,
			Keys.STATE_TEX_SIZE, size[0], size[1],
			Keys.STATE_SCALE, scale,
			Keys.STATE_ROTATION, y);
	}

	/** Deserializer for the {@link EntityState} class */
	public static class Deserializer implements JsonDeserializer<EntityState> {

		@Override
		public EntityState deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

			JsonObject jsonObj = json.getAsJsonObject();
			String modelStr = jsonObj.get(Keys.STATE_MODEL_NAME).getAsString();
			EntityModel model;
			if(Keys.MISSING_DEFINITION.equals(modelStr))
				model = EntityModel.MISSING_MODEL;
			else {
				ResourceLocation modelLocation = Util.getResourceLocation(modelStr, Keys.MODELS_DIR, Keys.JSON);
				try(Reader reader = Util.getReaderFor(modelLocation)) {
					model = EntityModel.GSON.fromJson(reader, EntityModel.class);
				} catch(IOException|JsonParseException e) {
					Logger.error(e, "Exception parsing model from entity state");
					return EntityModel.MISSING_STATE;
				}
			}
			EntityState res = new EntityState(model);
			if(jsonObj.has(Keys.STATE_TEXTURE)) {

				String tex = jsonObj.get(Keys.STATE_TEXTURE).getAsString();
				if(tex.equals(Keys.MISSING_DEFINITION))
					res.texture = Util.MISSING_LOCATION;
				else {
					res.texture = Util.getResourceLocation(tex, Keys.TEXTURES_DIR, Keys.PNG);
					res.texture = bindTexture(res.texture);
				}
			}
			if(jsonObj.has(Keys.STATE_TEX_SIZE)) {

				int[] temp = context.deserialize(jsonObj.get(Keys.STATE_TEX_SIZE), int[].class);
				res.size[0] = temp[0];
				res.size[1] = temp[1];
			}
			if(jsonObj.has(Keys.STATE_ROTATION))
				res.y = jsonObj.get(Keys.STATE_ROTATION).getAsFloat();
			if(jsonObj.has(Keys.STATE_SCALE))
				res.scale = jsonObj.get(Keys.STATE_SCALE).getAsFloat();
			return res;
		}
	}

	/** Class representing the "defaults" tag in an entitystate file */
	static class Defaults {

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