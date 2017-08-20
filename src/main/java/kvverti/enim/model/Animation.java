package kvverti.enim.model;

import java.io.InputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import kvverti.enim.Keys;
import kvverti.enim.Logger;
import kvverti.enim.Util;
import kvverti.enim.abiescript.AbieScript;
import kvverti.enim.abiescript.AnimationParser;
import kvverti.enim.abiescript.AbieParseException;
import kvverti.enim.entity.Entities;

public class Animation {

	private final AbieScript script;
	private final Map<String, String> defines;
	@Deprecated
	private final boolean scaleWithMovement;
	private final float scaling;
	private final float tuning;

	private Animation(AbieScript script, Map<String, String> defines, float scaling, float tuning) {

		this.script = script;
		this.defines = defines;
		this.scaleWithMovement = false;
		this.scaling = scaling;
		this.tuning = tuning;
	}

	public Set<String> defines() { return script.defines(); }

	public int frameCount() { return script.frameCount(); }

	public String toElementName(String defineName) {

		String result = defines.get(defineName);
		if(result == null) throw new IllegalArgumentException("No define: " + defineName);
		return result;
	}

	@Deprecated
	public boolean shouldScaleWithMovement() { return scaleWithMovement; }

	/** Scaling controls the animation's amplitude. Returns 0 if no scaling */
	public float scaling() { return scaling; }

	/** Tuning controls the animation's speed. Returns 0 if no tuning */
	public float tuning() { return tuning; }

	public AbieScript.Frame frame(int frame, float partial) {

		return script.frame(frame, partial);
	}

	@Override
	public String toString() {

		return "scaled: " + scaleWithMovement + ", elements: " + defines + ", script: [" + script + "]";
	}

	/** The Json deserializer for instances of {@code Animation}. */
	public static class Deserializer implements JsonDeserializer<Animation> {

		private static final java.lang.reflect.Type definesType = new TypeToken<Map<String, String>>(){}.getType();
		private static final AnimationParser parser = new AnimationParser();
		private static final Animation NOOP;
		static {

			final String script = "pause 1\n"; //empty AbieScript animation
			@SuppressWarnings("deprecation")
			InputStream input = new java.io.StringBufferInputStream(script);
			ResourceLocation loc = new ResourceLocation("enim:noop");
			IResource rsc = new SimpleResource("default", loc, input, null, new MetadataSerializer());
			AbieScript abiescript = parser.parse(rsc);
			NOOP = new Animation(abiescript, Collections.emptyMap(), 0.0f, 0.0f);
		}

		public Animation deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) {

			try {
				JsonObject obj = json.getAsJsonObject();
				ResourceLocation scriptLoc = Util.getResourceLocation(
					obj.get(Keys.ANIM_SCRIPT).getAsString(), Keys.ANIMS_DIR, Keys.ABIESCRIPT);
				IResource scriptFile = Entities.resourceManager().getResource(scriptLoc);
				AbieScript script = parser.parse(scriptFile);
				Map<String, String> defines = context.deserialize(obj.getAsJsonObject(Keys.ANIM_DEFINES), definesType);
				//boolean scaleWithMovement = obj.has(Keys.ANIM_SCALE_WITH_MOVEMENT) ?
				//	obj.getAsJsonPrimitive(Keys.ANIM_SCALE_WITH_MOVEMENT).getAsBoolean()
				//	: false;
				float scaling = obj.has(Keys.ANIM_VALUE_SCALE_WEIGHT) ?
					obj.getAsJsonPrimitive(Keys.ANIM_VALUE_SCALE_WEIGHT).getAsFloat()
					: 0.0f;
				float tuning = obj.has(Keys.ANIM_SPEED_SCALE_WEIGHT) ?
					obj.getAsJsonPrimitive(Keys.ANIM_SPEED_SCALE_WEIGHT).getAsFloat()
					: 0.0f;
				validate(script, defines);
				return new Animation(script, defines, scaling, tuning);

			} catch(IOException|AbieParseException e) {

				Logger.error(e);
				return NOOP;
			}
		}

		private void validate(AbieScript script, Map<String, String> defines) {

			//make sure defines contains mappings for all keys in the script
			if(!script.defines().equals(defines.keySet()))
				throw new JsonParseException("Defines in script and defines in model do not match");
		}
	}
}