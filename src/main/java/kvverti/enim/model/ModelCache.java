package kvverti.enim.model;

import java.io.Reader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

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
    private static final Map<ResourceLocation, EntityModel> entityModels = new HashMap<>(50);
    
    /** Stores entity armor models */
    private static final Map<ResourceLocation, ArmorModel> armorModels = new HashMap<>(5);
    
    /** Stores AbieScript animations */
    private static final Map<ResourceLocation, AbieScript> animationScripts = new HashMap<>(50);
    
    public static EntityModel getEntityModel(ResourceLocation location) {
        
        return entityModels.computeIfAbsent(location, ModelCache::parseEntityModel);
    }
    
    private static EntityModel parseEntityModel(ResourceLocation location) {
        
        try(Reader rd = Util.getReaderFor(location)) {
            return EntityModel.GSON.fromJson(rd, EntityModel.class);
        } catch(IOException|JsonParseException e) {
            Logger.error(e, "Exception parsing entity model");
            return EntityModel.MISSING_MODEL;
        }
    }
    
    public static ArmorModel getArmorModel(ResourceLocation location) {
        
        return armorModels.computeIfAbsent(location, ModelCache::parseArmorModel);
    }
    
    private static ArmorModel parseArmorModel(ResourceLocation location) {
        
        ArmorModel.JsonRepr repr;
        try { repr = parseArmorModelJson(location); }
        catch(IOException e) { return EntityModel.MISSING_ARMOR; }
        return new ArmorModel(repr);
    }
    
    private static ArmorModel.JsonRepr parseArmorModelJson(ResourceLocation location) throws IOException {
        
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
                Logger.error(e, "Exception parsing armor models");
            }
        }
        //combine with parent
        String parentName = repr.getParentName();
        if(parentName == null)
            return repr;
        ResourceLocation parent = Util.getResourceLocation(parentName, Keys.ARMOR_DIR, Keys.JSON);
        ArmorModel.JsonRepr parentRepr = parseArmorModelJson(parent);
        parentRepr.combineWith(repr);
        return parentRepr;
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
            Logger.error(e, "Exception parsing animation script");
            return EntityModel.MISSING_ABIESCRIPT;
        }
    }
    
    public static void clearCache() {
        
        Logger.info("Model cache loaded %d entity models, %d armor models, and %d animation scripts",
            entityModels.size(),
            armorModels.size(),
            animationScripts.size());
        entityModels.clear();
        armorModels.clear();
        animationScripts.clear();
    }
}