package kvverti.enim.model;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.annotations.SerializedName;

import kvverti.enim.Keys;
import kvverti.enim.Util;
import kvverti.enim.abiescript.AbieScript;
import kvverti.enim.abiescript.AnimationParser;
import kvverti.enim.entity.Entities;

public class Animation {

	private final AbieScript script;
	private final Map<String, String> defines;
	private final boolean scaleWithMovement;

	private Animation(AbieScript script, Map<String, String> defines, boolean scaleWithMovement) {

		this.script = script;
		this.defines = defines;
		this.scaleWithMovement = scaleWithMovement;
	}

	public Set<String> defines() { return script.defines(); }

	public int frameCount() { return script.frameCount(); }

	public String toElementName(String defineName) {

		String result = defines.get(defineName);
		if(result == null) throw new IllegalArgumentException("No define: " + defineName);
		return result;
	}

	public boolean shouldScaleWithMovement() { return scaleWithMovement; }

	public AbieScript.Frame frame(int frame) {

		return script.frame(frame);
	}

	@Override
	public String toString() {

		return "scaled: " + scaleWithMovement + ", elements: " + defines + ", script: [" + script + "]";
	}

	/** The conditions under which an animation may play */
	public enum Type {

		//These are arranged in increasing order of precedence
		@SerializedName("idle") IDLE,
		@SerializedName("moving") MOVE,
		@SerializedName("airborne") AIR,
		@SerializedName("swimming") SWIM,
		@SerializedName("tracking") TRACK,
		@SerializedName("jump") JUMP,
		@SerializedName("attack") ATTACK,
		@SerializedName("damage") DAMAGE,
		@SerializedName("greeting") GREET,
		@SerializedName("parting") PART
	}

	/** The Json deserializer for instances of {@code Animation}. */
	public static class Deserializer implements JsonDeserializer<Animation> {

		private static final java.lang.reflect.Type definesType = new TypeToken<Map<String, String>>(){}.getType();
		private static final AnimationParser parser = new AnimationParser();

		public Animation deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) {

			try {
				JsonObject obj = json.getAsJsonObject();
				ResourceLocation scriptLoc = Util.getResourceLocation(
					obj.get(Keys.ANIM_SCRIPT).getAsString(), Keys.ANIMS_DIR, Keys.ABIESCRIPT);
				IResource scriptFile = Entities.resourceManager().getResource(scriptLoc);
				AbieScript script = parser.parse(scriptFile);
				Map<String, String> defines = context.deserialize(obj.getAsJsonObject(Keys.ANIM_DEFINES), definesType);
				boolean scaleWithMovement = obj.has(Keys.ANIM_SCALE_WITH_MOVEMENT) ?
					obj.getAsJsonPrimitive(Keys.ANIM_SCALE_WITH_MOVEMENT).getAsBoolean()
					: false;
				return new Animation(script, defines, scaleWithMovement);

			} catch(IOException e) { throw new JsonIOException("IO-error parsing animation", e); }
		}
	}
}