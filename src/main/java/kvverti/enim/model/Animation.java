package kvverti.enim.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.ResourceLocation;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import kvverti.enim.Keys;
import kvverti.enim.Util;
import kvverti.enim.abiescript.AbieScript;

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
        private static final Animation NOOP =
            new Animation(EntityModel.MISSING_ABIESCRIPT, Collections.emptyMap(), 0.0f, 0.0f);

        public Animation deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) {

            JsonObject obj = json.getAsJsonObject();
            ResourceLocation scriptLoc = Util.getResourceLocation(
                obj.get(Keys.ANIM_SCRIPT).getAsString(), Keys.ANIMS_DIR, Keys.ABIESCRIPT);
            AbieScript script = ModelCache.getAbieScript(scriptLoc);
            if(script == EntityModel.MISSING_ABIESCRIPT)
                return NOOP;
            Map<String, String> defines = context.deserialize(obj.getAsJsonObject(Keys.ANIM_DEFINES), definesType);
            float scaling = obj.has(Keys.ANIM_VALUE_SCALE_WEIGHT) ?
                obj.getAsJsonPrimitive(Keys.ANIM_VALUE_SCALE_WEIGHT).getAsFloat()
                : 0.0f;
            float tuning = obj.has(Keys.ANIM_SPEED_SCALE_WEIGHT) ?
                obj.getAsJsonPrimitive(Keys.ANIM_SPEED_SCALE_WEIGHT).getAsFloat()
                : 0.0f;
            validate(script, defines);
            return new Animation(script, defines, scaling, tuning);
        }

        private void validate(AbieScript script, Map<String, String> defines) {

            //make sure defines contains mappings for all keys in the script
            if(!script.defines().equals(defines.keySet()))
                throw new JsonParseException("Defines in script and defines in model do not match");
        }
    }
}
