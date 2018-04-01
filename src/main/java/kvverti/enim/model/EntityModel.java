package kvverti.enim.model;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
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
import com.google.gson.reflect.TypeToken;

import kvverti.enim.Keys;
import kvverti.enim.Util;
import kvverti.enim.Vec3f;
import kvverti.enim.abiescript.AbieScript;
import kvverti.enim.abiescript.AnimationParser;
import kvverti.enim.entity.animation.AnimType;
import kvverti.enim.model.multipart.Condition;
import kvverti.enim.model.multipart.Rule;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.joining;

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
        .registerTypeAdapter(ModelImports.class, new ModelImports.Deserializer())
        .registerTypeAdapter(Animation.class, new Animation.Deserializer())
        .registerTypeAdapter(AnimType.class, new AnimType.Adapter().nullSafe())
        .registerTypeAdapter(EntityState.class, new EntityState.Deserializer())
        .registerTypeAdapter(new TypeToken<ImmutableList<EntityState>>(){}.getType(), new EntityState.ListDeserializer())
        .registerTypeAdapter(EntityStateMap.class, new EntityStateMap.Deserializer())
        .registerTypeAdapter(EntityModel.class, new EntityModel.Deserializer())
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
    public static final EntityModel MISSING_MODEL = GSON.fromJson(
        "{\"properties\":{\"nameplate\":18},\"elements\":[{\"name\":\"missingno\",\"from\":[0,0,0],\"to\":[16,16,16],\"uv\":[0,0]}]}",
        EntityModel.class);

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

    /** For Json deserialization */
    private EntityModel(ModelProperties properties, Set<ModelElement> elements, Map<AnimType, Animation> animations) {

        this.properties = properties;
        this.elements = ImmutableSet.copyOf(elements);
        this.animations = ImmutableMap.copyOf(animations);
        ImmutableMap.Builder<String, ModelElement> b = new ImmutableMap.Builder<>();
        for(ModelElement m : elements)
            b.put(m.name(), m);
        elementMap = b.build();
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

    /** Deserializer for the {@link EntityModel} class. */
    public static class Deserializer implements JsonDeserializer<EntityModel> {

        private static final Type elementsType = new TypeToken<Set<ModelElement>>(){}.getType();
        private static final Type animsType = new TypeToken<Map<AnimType, Animation>>(){}.getType();
        private static final Type overridesType = new TypeToken<Map<String, ModelElement.Override>>(){}.getType();
        private static final Type parentType = new TypeToken<List<String>>(){}.getType();

        @Override
        public EntityModel deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

            JsonObject obj = json.getAsJsonObject();
            //essential fields of the EntityModel class
            ModelProperties properties = obj.has(Keys.PROPERTIES_TAG) ?
                context.deserialize(obj.get(Keys.PROPERTIES_TAG), ModelProperties.class)
                : ModelProperties.DEFAULT;
            Set<ModelElement> elements = obj.has(Keys.ELEMENTS_TAG) ?
                context.deserialize(obj.get(Keys.ELEMENTS_TAG), elementsType)
                : new HashSet<>();
            Map<AnimType, Animation> animations = obj.has(Keys.ANIMS_TAG) ?
                context.deserialize(obj.get(Keys.ANIMS_TAG), animsType)
                : new LinkedHashMap<>();

            //other json objects that do not have an in-code representation
            if(obj.has(Keys.PARENT_TAG)) {
                List<String> parentModelNames = context.deserialize(obj.get(Keys.PARENT_TAG), parentType);
                //preserve declaration order of animations
                Map<AnimType, Animation> animTmp = new LinkedHashMap<>();
                for(String parentModelName : parentModelNames) {
                    ResourceLocation parentModelLoc =
                        Util.getResourceLocation(parentModelName, Keys.MODELS_DIR, Keys.JSON);
                    try(Reader rd = Util.getReaderFor(parentModelLoc)) {
                        EntityModel parent = GSON.fromJson(rd, EntityModel.class);
                        elements.addAll(parent.elements);
                        animTmp.putAll(parent.animations);
                        ModelProperties tmp = parent.properties;
                        tmp.combineWith(properties);
                        properties = tmp;
                    } catch(IOException e) {
                        throw new JsonParseException(e);
                    }
                }
                animTmp.putAll(animations);
                animations = animTmp;
            }
            if(obj.has(Keys.IMPORTS_TAG)) {

                ModelImports imports = context.deserialize(obj.get(Keys.IMPORTS_TAG), ModelImports.class);
                elements.addAll(imports.elements);
                //preserve declaration order
                imports.animations.putAll(animations);
                animations = imports.animations;
            }
            if(obj.has(Keys.ELEMENTS_OVERRIDES)) {

                Map<String, ModelElement.Override> overrides =
                    context.deserialize(obj.get(Keys.ELEMENTS_OVERRIDES), overridesType);
                for(ModelElement element : elements)
                    element.applyOverride(overrides.get(element.name()));
            }
            validate(properties, elements, animations);
            return new EntityModel(properties, elements, animations);
        }

        private void validate(ModelProperties properties, Set<ModelElement> elements, Map<AnimType, Animation> animations) {

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