package kvverti.enim.model;

import java.lang.reflect.Type;
import java.io.InputStream;
import java.io.Reader;
import java.io.IOException;
import javax.imageio.ImageIO;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import com.google.gson.*;

import kvverti.enim.Keys;
import kvverti.enim.Logger;
import kvverti.enim.Util;
import kvverti.enim.entity.Entities;

/** An entry in the "states" tag of an entitystate file. */
public class EntityState {

	//optional elements will be filled in by the defaults if left with these "invalid" values
	public final EntityModel model;		//will NOT be filled, error if not specified
	public ResourceLocation texture = null;	//invalid value (null) will be filled with default
	public final int[] size = { -1, -1 };	//invalid value (-1) will be filled with default
	public float scale = Float.NaN;		//invalid value (NaN) will be filled with default
	public float y = Float.NaN;		//invalid value (NaN) will be filled with default

	/** For Json deserialization */
	private EntityState(EntityModel model) { this.model = model; }

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

	@Override
	public String toString() {

		return String.format("EntityState { \"%s\", %s, \"%s\": %s, \"%s\": [%d, %d], \"%s\": %f, \"%s\": %f }",
			Keys.STATE_MODEL_NAME, model,
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
			ResourceLocation modelLocation = Util.getResourceLocation(modelStr, Keys.MODELS_DIR, Keys.JSON);
			EntityModel model;
			try(Reader reader = Util.getReaderFor(modelLocation)) {
				model = EntityModel.GSON.fromJson(reader, EntityModel.class);
			} catch(IOException e) { throw new JsonIOException("IO-error parsing model from entity state", e); }
			EntityState res = new EntityState(model);
			res.texture = jsonObj.has(Keys.STATE_TEXTURE) ?
				Util.getResourceLocation(jsonObj.get(Keys.STATE_TEXTURE).getAsString(), Keys.TEXTURES_DIR, Keys.PNG)
				: null;
			if(res.texture != null)
				res.texture = bindTexture(res.texture);
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
}