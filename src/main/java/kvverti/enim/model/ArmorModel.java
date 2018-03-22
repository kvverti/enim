package kvverti.enim.model;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.HashMap;

import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import kvverti.enim.Keys;

/**
 * ArmorModels store the armor models for a particular entity state. The models
 * may be specified individually using an EntityState, or a default state may
 * be specified for all other armor, which uses a texture taken from the
 * armor material.
 */
public class ArmorModel {
    
    /** Stores the default models */
    private Map<EntityEquipmentSlot, EntityState> defaults = new HashMap<>();
    
    /** Stores any individual models */
    private Map<ArmorMaterial, Map<EntityEquipmentSlot, ImmutableList<EntityState>>> materials = new HashMap<>();
    
    /** Creates an armor model with no mappings */
    ArmorModel() { }
    
    /** Creates an armor model from the given representation */
    ArmorModel(JsonRepr repr) {
        
        defaults = repr.defaults;
        materials = repr.materials;
        init();
    }
    
    private final void init() {
        
        EntityState.Defaults def = new EntityState.Defaults();
        for(EntityState s : defaults.values())
            s.replaceDefaults(def);
        for(Map<EntityEquipmentSlot, ImmutableList<EntityState>> tmp : materials.values())
            for(ImmutableList<EntityState> ls : tmp.values())
                for(EntityState s : ls)
                    s.replaceDefaults(def);
    }
    
    public ImmutableList<EntityState> getArmorLayers(ArmorMaterial material, EntityEquipmentSlot slot) {
        
        if(materials.containsKey(material)) {
            Map<EntityEquipmentSlot, ImmutableList<EntityState>> tmp = materials.get(material);
            if(tmp.containsKey(slot)) {
                return tmp.get(slot);
            }
        }
        if(!defaults.containsKey(slot))
            return ImmutableList.of(EntityModel.MISSING_STATE);
        ResourceLocation texture = new ResourceLocation(armorTexture(material, slot));
        EntityState state = defaults.get(slot).replaceTexture(texture);
        return ImmutableList.of(state);
    }
    
    private String armorTexture(ArmorMaterial material, EntityEquipmentSlot slot) {
        
        final String template = "%s:textures/models/armor/%s_layer_%d.png";
        ResourceLocation name = new ResourceLocation(material.getName());
        return String.format(template,
            name.getResourceDomain(),
            name.getResourcePath(),
            slot == EntityEquipmentSlot.LEGS ? 2 : 1);
    }
    
    public static class MaterialDeserializer implements JsonDeserializer<ArmorMaterial> {
        
        private static final ArmorMaterial NONE = ArmorMaterial.valueOf("ENIM_NONE");
        static { assert NONE != null; }
        
        private static final ImmutableMap<String, ArmorMaterial> materials;
        static {
            ImmutableMap.Builder<String, ArmorMaterial> b = ImmutableMap.builder();
            for(ArmorMaterial m : ArmorMaterial.values())
                b.put(m.getName(), m);
            materials = b.build();
        }
        
        @Override
        public ArmorMaterial deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            
            return materials.getOrDefault(json.getAsString(), NONE);
        }
    }
    
    public static class SlotDeserializer implements JsonDeserializer<EntityEquipmentSlot> {
        
        private static final ImmutableMap<String, EntityEquipmentSlot> slots;
        static {
            ImmutableMap.Builder<String, EntityEquipmentSlot> b = ImmutableMap.builder();
            for(EntityEquipmentSlot s : EntityEquipmentSlot.values())
                b.put(s.getName(), s);
            slots = b.build();
        }
        
        @Override
        public EntityEquipmentSlot deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            
            String s = json.getAsString();
            if(!slots.containsKey(s))
                throw new JsonParseException("No such EntityEquipmentSlot " + s);
            return slots.get(s);
        }
    }
    
    /** Used to deserialize armor models from JSON. */
    static class JsonRepr {
        
        /** This armor model's parent, as a string */
        @SerializedName(Keys.ARMOR_PARENT)
        private String parent = null;
        
        /** Stores the default models */
        @SerializedName(Keys.ARMOR_DEFAULTS)
        private Map<EntityEquipmentSlot, EntityState> defaults = new HashMap<>();
        
        /** Stores any individual models */
        @SerializedName(Keys.ARMOR_MATERIALS)
        private Map<ArmorMaterial, Map<EntityEquipmentSlot, ImmutableList<EntityState>>> materials = new HashMap<>();
        
        String getParentName() {
            
            return parent;
        }
    
        /** Adds to or replaces this instance's data with other's data. */
        void combineWith(JsonRepr other) {
            
            parent = other.parent;
            defaults.putAll(other.defaults);
            for(Map.Entry<ArmorMaterial, Map<EntityEquipmentSlot, ImmutableList<EntityState>>> entry
                : other.materials.entrySet()) {
                //combine other's materials with this's materials
                if(materials.containsKey(entry.getKey()))
                    materials.get(entry.getKey()).putAll(entry.getValue());
                else
                    materials.put(entry.getKey(), entry.getValue());
            }
        }
    }
}