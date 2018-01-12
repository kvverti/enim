package kvverti.enim.model;

import java.lang.reflect.Type;
import java.io.InputStream;
import java.io.Reader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.imageio.ImageIO;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import kvverti.enim.Keys;
import kvverti.enim.Logger;
import kvverti.enim.Util;
import kvverti.enim.entity.Entities;

/** An entry in the {@value Keys#STATES_TAG} tag of an entitystate file. */
public class EntityState {

    //optional elements will be filled in by the defaults if left with these "invalid" values
    private final EntityModel model;        //will NOT be filled, error if not specified
    private ResourceLocation texture = null;    //invalid value (null) will be filled with default
    private ResourceLocation overlay = null;    //invalid value (null) will be filled with default
    private final int[] size = { -1, -1 };        //invalid value (-1) will be filled with default
    private float scale = Float.NaN;        //invalid value (NaN) will be filled with default
    private float y = Float.NaN;            //invalid value (NaN) will be filled with default
    private ArmorModel armor; //armor model for this state. May be null if there is no armor.

    //original texture names, for debugging purposes
    private String textureName;
    private String overlayName;

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
     * The texture overlay for this state. The overlay is used for non-shaded textures like spider and endermen eyes. The returned
     * ResourceLocation is a dynamic location, so it will not reflect the file path of the actual texture image. Returns null in the
     * case where there is no overlay texture.
     */
    public ResourceLocation overlay() { return overlay; }

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
    
    /**
     * The armor models for this state. Armor models are rendered when the entity is wearing the corresponding
     * armor piece. Returns null if this state does not render armor.
     */
    public ArmorModel armor() { return armor; }

    /** Binds a texture dynamically. This is needed because the texture may be reloaded many times over the course of a game session. */
    private static ResourceLocation bindTexture(ResourceLocation loc) {

        Entities.textureManager().bindTexture(loc);
        return loc;
    }

    /* For use by EntityStateMap */
    void replaceDefaults(Defaults defaults) {

        if(texture == null)
            texture = defaults.texture == null ?
                Util.MISSING_LOCATION
                : Util.getResourceLocation(defaults.texture, Keys.TEXTURES_DIR, Keys.PNG);
        if(overlay == null && defaults.overlay != null)
            overlay = Util.getResourceLocation(defaults.overlay, Keys.TEXTURES_DIR, Keys.PNG);
        if(size[0] == -1 && size[1] == -1) {

            size[0] = defaults.size[0];
            size[1] = defaults.size[1];
        }
        if(Float.isNaN(scale))
            scale = defaults.scale;
        if(Float.isNaN(y))
            y = defaults.y;
    }
    
    EntityState replaceTexture(ResourceLocation texture) {
        
        EntityState res = new EntityState(model);
        res.texture = texture;
        res.overlay = overlay;
        res.size[0] = size[0];
        res.size[1] = size[1];
        res.scale = scale;
        res.y = y;
        res.armor = armor;
        return res;
    }

    @Override
    public String toString() {

        return String.format("EntityState { \"%s\", %s, \"%s\": %s, \"%s\": %s, \"%s\": [%d, %d], \"%s\": %f, \"%s\": %f }",
            Keys.STATE_MODEL_NAME, "[elided]",
            Keys.STATE_TEXTURE, textureName,
            Keys.STATE_OVERLAY, overlayName,
            Keys.STATE_TEX_SIZE, size[0], size[1],
            Keys.STATE_SCALE, scale,
            Keys.STATE_ROTATION, y);
    }
    
    /** Deserializer for lists of EntityStates. */
    public static class ListDeserializer implements JsonDeserializer<ImmutableList<EntityState>> {
        
        private static final Type stateListType = new TypeToken<List<EntityState>>(){}.getType();

        @Override
        public ImmutableList<EntityState> deserialize(JsonElement json, Type type, JsonDeserializationContext context) {

            if(json.isJsonArray()) {

                if(json.getAsJsonArray().size() == 0)
                    throw new JsonParseException("Layers list may not be empty");
                List<EntityState> list = context.deserialize(json, stateListType);
                return ImmutableList.copyOf(list);
            } else
                return ImmutableList.of(context.deserialize(json, EntityState.class));
        }
    }

    /** Deserializer for the {@link EntityState} class */
    public static class Deserializer implements JsonDeserializer<EntityState> {

        @Override
        public EntityState deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            
            JsonObject jsonObj = json.getAsJsonObject();
            String modelStr = jsonObj.get(Keys.STATE_MODEL_NAME).getAsString();
            EntityModel model;
            if(Keys.MISSING_DEFINITION.equals(modelStr))
                model = EntityModel.MISSING_MODEL;
            else {
                ResourceLocation modelLocation = Util.getResourceLocation(modelStr, Keys.MODELS_DIR, Keys.JSON);
                model = ModelCache.getEntityModel(modelLocation);
                if(model == EntityModel.MISSING_MODEL)
                    return EntityModel.MISSING_STATE;
            }
            EntityState res = new EntityState(model);
            if(jsonObj.has(Keys.STATE_TEXTURE)) {

                String tex = jsonObj.get(Keys.STATE_TEXTURE).getAsString();
                if(tex.equals(Keys.MISSING_DEFINITION)) {

                    res.texture = Util.MISSING_LOCATION;
                    res.textureName = Util.MISSING_LOCATION.toString();
                } else {
                    res.texture = Util.getResourceLocation(tex, Keys.TEXTURES_DIR, Keys.PNG);
                    res.textureName = res.texture.toString();
                    res.texture = bindTexture(res.texture);
                }
            }
            if(jsonObj.has(Keys.STATE_OVERLAY)) {

                String tex = jsonObj.get(Keys.STATE_OVERLAY).getAsString();
                res.overlay = bindTexture(Util.getResourceLocation(tex, Keys.TEXTURES_DIR, Keys.PNG));
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
            yuck:
            if(jsonObj.has(Keys.STATE_ARMOR)) {
                ResourceLocation armorFile =
                    Util.getResourceLocation(jsonObj.get(Keys.STATE_ARMOR).getAsString(), Keys.ARMOR_DIR, Keys.JSON);
                res.armor = ModelCache.getArmorModel(armorFile);
            }
            return res;
        }
    }

    /** Class representing the "defaults" tag in an entitystate file */
    static class Defaults {

        @SerializedName(Keys.STATE_TEXTURE)
        String texture = null;

        @SerializedName(Keys.STATE_OVERLAY)
        String overlay = null;

        @SerializedName(Keys.STATE_TEX_SIZE)
        final int[] size = { 64, 32 };

        @SerializedName(Keys.STATE_SCALE)
        float scale = 1.0f;

        @SerializedName(Keys.STATE_ROTATION)
        float y = 0.0f;
    }
}