package kvverti.enim.model;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

import net.minecraftforge.fml.common.registry.ForgeRegistries;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import kvverti.enim.Keys;
import kvverti.enim.Vec3f;
import kvverti.enim.abiescript.AbieScript;
import kvverti.enim.abiescript.AnimationParser;
import kvverti.enim.entity.animation.AnimType;
import kvverti.enim.model.multipart.Condition;
import kvverti.enim.model.multipart.Rule;

import static java.util.stream.Collectors.toSet;

/**
 * This class represents the contents of an entity model file, located in the {@value Keys#MODELS_DIR} directory. Properties
 * of class instances correspond approximately one-to-one with the model format representation, except that imported elements
 * are resolved and overrides are applied.
 */
public class EntityModel {

    /**
     * The Gson instance used to deserialize Enim models and AbieScript animations. Note that many of these classes are deserialized lossily,
     * they cannot be serialized into Json again.
     */
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(ArmorMaterial.class, new ArmorModel.MaterialDeserializer())
        .registerTypeAdapter(EntityEquipmentSlot.class, new ArmorModel.SlotDeserializer())
        .registerTypeAdapter(Vec3f.class, new Vec3f.Adapter().nullSafe())
        .registerTypeAdapter(Animation.class, new Animation.Deserializer())
        .registerTypeAdapter(AnimType.class, new AnimType.Adapter().nullSafe())
        .registerTypeAdapter(EntityState.class, new EntityState.Deserializer())
        .registerTypeAdapter(new TypeToken<ImmutableList<EntityState>>(){}.getType(), new EntityState.ListDeserializer())
        .registerTypeAdapter(EntityStateMap.class, new EntityStateMap.Deserializer())
        .registerTypeAdapter(Condition.class, new Condition.Deserializer())
        .registerTypeAdapter(Rule.class, new Rule.Deserializer())
        .registerTypeAdapter(ScaleProperty.class, new ScaleProperty.Deserializer())
        .registerTypeAdapter(Item.class, new ItemDeserializer())
        .registerTypeAdapter(IBlockState.class, new BlockStateDeserializer())
        .registerTypeAdapter(ElementType.class, new ElementType.Deserializer())
        .create();

    /**
     * The invalid ("missingno") entity model.
     */
    public static final EntityModel MISSING_MODEL = new EntityModel(GSON.fromJson(
        "{\"properties\":{\"nameplate\":18},\"elements\":[{\"name\":\"missingno\",\"from\":[0,0,0],\"to\":[16,16,16],\"uv\":[0,0]}]}",
        EntityModel.JsonRepr.class));

    /**
     * The invalid ("missingno") entity state.
     */
    public static final EntityState MISSING_STATE = GSON.fromJson(
        "{\"model\":\"builtin/missingno\",\"texture\":\"builtin/missingno\",\"size\":[64,32],\"y\":0,\"scale\":1}",
        EntityState.class);

    /**
     * The invalid ("missingno") armor model.
     */
    public static final ArmorModel MISSING_ARMOR = new ArmorModel();

    /**
     * The invalid ("missingno") AbieScript animation.
     */
    public static final AbieScript MISSING_ABIESCRIPT;
    static {

        @SuppressWarnings("deprecation")
        AnimationParser parser = kvverti.enim.EnimRenderingRegistry.getGlobalParser();
        final String script = "pause 1\n"; //empty AbieScript animation
        @SuppressWarnings("deprecation")
        InputStream input = new java.io.StringBufferInputStream(script);
        ResourceLocation loc = new ResourceLocation("enim:noop");
        IResource rsc = new SimpleResource("default", loc, input, null, new MetadataSerializer());
        MISSING_ABIESCRIPT = parser.parse(rsc);
    }

    private final ModelProperties properties;
    private final ImmutableSet<ModelElement> elements;
    private final ImmutableMap<String, ModelElement> elementMap;
    private final ImmutableMap<AnimType, Animation> animations;

    /** Create an EntityModel from its JSON representation */
    EntityModel(JsonRepr repr) {

        this.properties = repr.properties;
        this.elements = ImmutableSet.copyOf(repr.elements);
        this.animations = ImmutableMap.copyOf(repr.animations);
        ImmutableMap.Builder<String, ModelElement> b = new ImmutableMap.Builder<>();
        for(ModelElement m : elements)
            b.put(m.name(), m);
        if(!repr.overrides.isEmpty()) {
            for(ModelElement element : elements)
                element.applyOverride(repr.overrides.get(element.name()));
        }
        elementMap = b.build();
        validate();
    }

    /**
     * This model's properties. The model properties contain miscellaneous data about the model, such as nameplate height and held item
     * positions.
     * @return this model's properties
     * @see ModelProperties
     */
    public ModelProperties properties() { return properties; }

    /**
     * This model's elements. The model elements are cuboid and contain data such as size, position, and texture coordinates. To get a
     * specific element, the {@link #getElement(String)} method should be used instead.
     * @return an immutable set containing this model's elements
     * @see ModelElement
     * @see #getElement(String)
     */
    public ImmutableSet<ModelElement> elements() { return elements; }

    /**
     * This model's animations. The model animations control the transformations of the elements under certain conditions, such as movement
     * or interaction. If there is no animation for a given animation type, is will not be present in the returned map.
     * @return an immutable map containing this model's animations
     * @see Animation
     */
    public ImmutableMap<AnimType, Animation> animations() { return animations; }

    /**
     * Returns the element in this model with the given name. The name must match exactly with an element in this model. To get all
     * elements, use {@link #elements()}.
     * @param name the name of the element to return
     * @return the element with the given name
     * @throws NoSuchElementException if this model does not contain an element with the given name
     */
    public ModelElement getElement(String name) {

        if(elementMap.containsKey(name))
            return elementMap.get(name);
        throw new NoSuchElementException(name);
    }

    @Override
    public String toString() {

        return String.format("EntityModel { \"%s\": %s, \"%s\": %s, \"%s\": %s }",
            Keys.PROPERTIES_TAG, properties,
            Keys.ELEMENTS_TAG, elements,
            Keys.ANIMS_TAG, animations);
    }

    private void validate() {

        Set<String> elementNames = elements.stream().map(ModelElement::name).collect(toSet());
        //make sure element parents reference valid elements, and validate elements
        for(ModelElement elem : elements) {

            elem.verify();
            ensureContains(elem.parent(), elementNames);
        }
        //make sure properties reference valid elements
        ensureValidOrigin(properties.helmet(), elementNames);
        ensureValidOrigin(properties.leftHand(), elementNames);
        ensureValidOrigin(properties.rightHand(), elementNames);
        //make sure animations defines reference valid elements
        for(Animation anim : animations.values()) {

            Set<String> elemNames = anim.defines().stream().map(anim::toElementName).collect(toSet());
            for(String name : elemNames)
                ensureContains(name, elementNames);
        }
    }

    private void ensureValidOrigin(ModelProperties.OriginPoint p, Set<String> names) {

        if(p != null)
            ensureContains(p.parent(), names);
    }

    private void ensureContains(String name, Set<String> names) {

        if(name != null && !name.isEmpty() && !names.contains(name))
            throw new JsonParseException(String.format("Element %s does not exist", name));
    }

    /** Class for helping deserialization */
    static class JsonRepr {

        @SerializedName(Keys.PARENT_TAG)
        List<String> parents = new ArrayList<>();

        @SerializedName(Keys.PROPERTIES_TAG)
        ModelProperties properties = new ModelProperties();

        @SerializedName(Keys.ELEMENTS_TAG)
        Set<ModelElement> elements = new HashSet<>();

        @SerializedName(Keys.ANIMS_TAG)
        Map<AnimType, Animation> animations = new LinkedHashMap<>();

        @SerializedName(Keys.ELEMENTS_OVERRIDES)
        Map<String, ModelElement.Override> overrides = new HashMap<>();

        void combineWith(JsonRepr other) {

            other.elements.addAll(elements);
            elements = other.elements;
            animations.putAll(other.animations);
            properties.combineWith(other.properties);
            overrides.putAll(other.overrides);
        }
    }

    /** Deserializer for items */
    private static class ItemDeserializer implements JsonDeserializer<Item> {

        @Override
        public Item deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

            String itemStr = json.getAsString();
            if(!Keys.RESOURCE_LOCATION_REGEX.matcher(itemStr).matches())
                throw new JsonParseException("Invalid item " + itemStr);
            ResourceLocation itemId = new ResourceLocation(itemStr);
            Item res = ForgeRegistries.ITEMS.getValue(itemId);
            if(res == null)
                throw new JsonParseException("Item " + itemId + " does not exist");
            return res;
        }
    }

    /** Deserializer for blocks */
    private static class BlockStateDeserializer implements JsonDeserializer<IBlockState> {

        private static final Type blockStateMapType = new TypeToken<Map<String, String>>(){}.getType();

        @Override
        public IBlockState deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

            JsonObject obj = json.getAsJsonObject();
            ResourceLocation blockId =
                new ResourceLocation(obj.get(Keys.ELEM_BLOCKSTATE_BLOCK).getAsString());
            Block block = ForgeRegistries.BLOCKS.getValue(blockId);
            if(block == null)
                throw new JsonParseException("Block " + blockId + " does not exist");
            IBlockState res = block.getDefaultState();
            if(obj.has(Keys.ELEM_BLOCKSTATE_STATE)) {
                Map<String, String> blockStateMap =
                    context.deserialize(obj.get(Keys.ELEM_BLOCKSTATE_STATE), blockStateMapType);
                for(IProperty<?> property : res.getPropertyKeys()) {
                    String tmp = blockStateMap.get(property.getName());
                    if(tmp != null)
                        res = placeProperty(property, tmp, res);
                }
            }
            return res;
        }

        private <T extends Comparable<T>>
            IBlockState placeProperty(IProperty<T> property, String strValue, IBlockState in) {

            Optional<T> value = property.parseValue(strValue);
            if(!value.isPresent())
                throw new JsonParseException("Invalid value " + strValue + " for property " + property.getName());
            return in.withProperty(property, value.get());
        }
    }
}
