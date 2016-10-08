package kvverti.enim.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Type;

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

	private Animation(AbieScript script, Map<String, String> defines) {

		this.script = script;
		this.defines = defines;
	}

	public Set<String> defines() { return script.defines(); }

	public int frameCount() { return script.frameCount(); }

	public String toElementName(String defineName) {

		String result = defines.get(defineName);
		if(result == null) throw new IllegalArgumentException("No define: " + defineName);
		return result;
	}

	public AbieScript.Frame frame(int frame) {

		return script.frame(frame);
	}

	@Override
	public String toString() {

		return "elements: " + defines + ", script: [" + script + "]";
	}

	/** The conditions under which an animation may play */
	public enum Type {

		//These are arranged in increasing order of precedence
		@SerializedName("idle") IDLE,
		@SerializedName("moving") MOVE,
		@SerializedName("airborne") AIR,
		@SerializedName("swimming") SWIM,
		@SerializedName("jump") JUMP,
		@SerializedName("attack") ATTACK,
		@SerializedName("damage") DAMAGE,
		@SerializedName("greeting") GREET,
		@SerializedName("parting") PART
	}

	/** The Json deserializer for instances of {@code Animation}. */
	public static class Deserializer implements JsonDeserializer<Animation> {

		private static final java.lang.reflect.Type definesType = new TypeToken<Map<String, String>>(){}.getType();

		public Animation deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) {

			try {
				ResourceLocation scriptLoc = Util.getResourceLocation(
					json.getAsJsonObject().get(Keys.ANIM_SCRIPT).getAsString(), Keys.ANIMS_DIR, Keys.ABIESCRIPT);
				IResource scriptFile = Entities.resourceManager().getResource(scriptLoc);
				AbieScript script = new AnimationParser(scriptFile).parse();
				Map<String, String> defines = context.deserialize(json.getAsJsonObject().getAsJsonObject(Keys.ANIM_DEFINES), definesType);
				return new Animation(script, defines);

			} catch(IOException e) { throw new JsonIOException("IO-error parsing animation", e); }
		}
	}
}