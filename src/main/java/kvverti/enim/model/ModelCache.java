package kvverti.enim.model;

import java.io.Reader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import com.google.gson.JsonParseException;

import kvverti.enim.Keys;
import kvverti.enim.Util;
import kvverti.enim.Logger;
import kvverti.enim.abiescript.AbieScript;
import kvverti.enim.abiescript.AbieParseException;
import kvverti.enim.abiescript.AnimationParser;
import kvverti.enim.entity.Entities;

/**
 * Stores model system objects and their associated resource locations.
 */
public final class ModelCache {
    
    /** Stores entity JSON models */
    private static final Map<ResourceLocation, EntityModel> entityModels = new HashMap<>(75);
    
    /** Stores entity JSON model representations */
    private static final Map<ResourceLocation, EntityModel.JsonRepr> entityModelJsons = new HashMap<>(75);
    
    /** Stores entity armor models */
    private static final Map<ResourceLocation, ArmorModel> armorModels = new HashMap<>(5);
    
    /** Stores entity armor model representations */
    private static final Map<ResourceLocation, ArmorModel.JsonRepr> armorModelJsons = new HashMap<>(5);
    
    /** Stores AbieScript animations */
    private static final Map<ResourceLocation, AbieScript> animationScripts = new HashMap<>(50);
    
    public static EntityModel getEntityModel(ResourceLocation location) {
        
        return entityModels.computeIfAbsent(location, ModelCache::parseEntityModel);
    }
    
    private static EntityModel parseEntityModel(ResourceLocation location) {
        
        EntityModel.JsonRepr repr;
        try { repr = parseEntityModelJson(location, new HashSet<>()); }
        catch(IOException|JsonParseException e) {
            Logger.error(e, "Exception parsing entity model " + location);
            return EntityModel.MISSING_MODEL;
        }
        return new EntityModel(repr);
    }
    
    private static EntityModel.JsonRepr parseEntityModelJson(ResourceLocation location,
        Set<ResourceLocation> seen) throws IOException {
        
        if(entityModelJsons.containsKey(location))
            return entityModelJsons.get(location);
        if(!seen.add(location))
            throw new JsonParseException("Circular model reference");
        EntityModel.JsonRepr resRepr = new EntityModel.JsonRepr();
        EntityModel.JsonRepr lastRepr;
        try(Reader rd = Util.getReaderFor(location)) {
            lastRepr = EntityModel.GSON.fromJson(rd, EntityModel.JsonRepr.class);
            lastRepr.init();
        }
        for(String name : lastRepr.parents) {
            ResourceLocation loc = Util.getResourceLocation(name, Keys.MODELS_DIR, Keys.JSON);
            EntityModel.JsonRepr parentRepr = parseEntityModelJson(loc, seen);
            parentRepr.init();
            resRepr.combineWith(parentRepr);
        }
        resRepr.combineWith(lastRepr);
        entityModelJsons.put(location, resRepr);
        return resRepr;
    }
    
    public static ArmorModel getArmorModel(ResourceLocation location) {
        
        return armorModels.computeIfAbsent(location, ModelCache::parseArmorModel);
    }
    
    private static ArmorModel parseArmorModel(ResourceLocation location) {
        
        ArmorModel.JsonRepr repr;
        try { repr = parseArmorModelJson(location, new HashSet<>()); }
        catch(IOException e) { return EntityModel.MISSING_ARMOR; }
        return new ArmorModel(repr);
    }
    
    private static ArmorModel.JsonRepr parseArmorModelJson(ResourceLocation location,
        Set<ResourceLocation> seen) throws IOException {
        
        if(armorModelJsons.containsKey(location))
            return armorModelJsons.get(location);
        if(!seen.add(location))
            throw new JsonParseException("Circular armor model reference");
        //attempt to open file
        //if file cannot be opened, fail the entire parse
        List<IResource> resources;
        try { resources = Entities.resourceManager().getAllResources(location); }
        catch(IOException e) {
            Logger.error(e, "Could not open armor model " + location);
            throw e;
        }
        ArmorModel.JsonRepr repr = new ArmorModel.JsonRepr();
        //flatten files from all resource packs
        for(IResource rsc : resources) {
            try(Reader rd = Util.getReaderFor(rsc)) {
                ArmorModel.JsonRepr tmp = EntityModel.GSON.fromJson(rd, ArmorModel.JsonRepr.class);
                repr.combineWith(tmp);
            } catch(IOException|JsonParseException e) {
                Logger.error(e, "Exception parsing armor models " + location);
            }
        }
        //combine with parent
        String parentName = repr.getParentName();
        if(parentName != null) {
            ResourceLocation parent = Util.getResourceLocation(parentName, Keys.ARMOR_DIR, Keys.JSON);
            ArmorModel.JsonRepr parentRepr = parseArmorModelJson(parent, seen);
            parentRepr.combineWith(repr);
            repr = parentRepr;
        }
        armorModelJsons.put(location, repr);
        return repr;
    }
    
    public static AbieScript getAbieScript(ResourceLocation location) {
        
        return animationScripts.computeIfAbsent(location, ModelCache::parseAbieScript);
    }
    
    private static AbieScript parseAbieScript(ResourceLocation location) {
        
        @SuppressWarnings("deprecation")
        AnimationParser parser = kvverti.enim.EnimRenderingRegistry.getGlobalParser();
        try {
            IResource scriptFile = Entities.resourceManager().getResource(location);
            return parser.parse(scriptFile);
        } catch(IOException|AbieParseException e) {
            Logger.error(e, "Exception parsing animation script " + location);
            return EntityModel.MISSING_ABIESCRIPT;
        }
    }
    
    public static void clearCache() {
        
        Logger.info("Model cache loaded %d entity models in %d files, "
            + "%d armor models in %d files, and %d animation scripts",
            entityModels.size(),
            entityModelJsons.size(),
            armorModels.size(),
            armorModelJsons.size(),
            animationScripts.size());
        entityModels.clear();
        entityModelJsons.clear();
        armorModels.clear();
        armorModelJsons.clear();
        animationScripts.clear();
    }
}