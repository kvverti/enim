package kvverti.enim;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Function;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import com.google.gson.JsonParseException;

import kvverti.enim.abiescript.AbieParseException;
import kvverti.enim.abiescript.AnimationParser;
import kvverti.enim.entity.ReloadableRender;
import kvverti.enim.entity.Entities;
import kvverti.enim.entity.color.CustomDyeColor;
import kvverti.enim.model.EntityStateMap;
import kvverti.enim.model.ModelCache;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Registry for ENIM related rendering features. This includes all entity and tile entity renders as well as
 * anything else that ENIM must be made aware of.
 */
public final class EnimRenderingRegistry {

    /** The singleton instance */
    private static final EnimRenderingRegistry registry = new EnimRenderingRegistry();

    /** Stores all registered renders so they can be reloaded */
    private final Map<ResourceLocation, ReloadableRender> renders = new HashMap<>(20);

    /** The one AnimationParser to rule them all. In seriousness, it stores all the functions. */
    private final AnimationParser animParser = new AnimationParser();

    /** Stores all domains registered with Enim. */
    private final Set<String> domains = new HashSet<>(5);

    /** Construction disallowed */
    private EnimRenderingRegistry() { }

    /**
     * Registers a reloadable entity renderer. Renders registered through this method will be reloaded with the game resources
     * when resource packs change, in addition to being registered with Forge. This method should be called during the
     * preinitialization phase.
     * @param <T> The type of entity
     * @param <R> The type of render
     * @param cls The entity class which will be rendered
     * @param factory A factory that returns a reloadable entity render with the given RenderManager
     */
    public static <T extends Entity, R extends Render<? super T> & ReloadableRender>
        void registerEntityRender(Class<T> cls, String modDomain, String entityId, Function<? super RenderManager, ? extends R> factory) {

        checkNotNull(modDomain);
        checkNotNull(entityId);
        checkNotNull(factory);
        registry.domains.add(modDomain);
        RenderingRegistry.registerEntityRenderingHandler(cls, manager -> {

            R r = checkNotNull(factory.apply(manager));
            registry.renders.put(new ResourceLocation(modDomain, Keys.STATES_DIR + entityId + Keys.JSON), r);
            return r;
        });
    }

    /**
     * Registers a reloadable tile entity renderer. Renders registered through this method will be reloaded with the game
     * resources when resource packs change, in addition to being registered with Forge. This method should be called during
     * the initialization phase.
     * @param <T> The type of tile entity
     * @param <R> The type of renderer
     * @param cls The tile entity class which will be rendered
     * @param render The renderer
     */
    public static <T extends TileEntity, R extends TileEntitySpecialRenderer<? super T> & ReloadableRender>
        void registerTileEntityRender(Class<T> cls, String modDomain, String entityId, R render) {

        checkNotNull(modDomain);
        checkNotNull(entityId);
        checkNotNull(render);
        registry.domains.add(modDomain);
        ClientRegistry.bindTileEntitySpecialRenderer(cls, render);
        registry.renders.put(new ResourceLocation(modDomain, Keys.STATES_DIR + entityId + Keys.JSON), render);
    }

    /**
     * @deprecated Only exists so Animation deserializer can access the parser.
     */
    @Deprecated
    public static AnimationParser getGlobalParser() { return registry.animParser; }

    /** EventHandler - called during init phase */
    static void init(FMLInitializationEvent e) {

            Entities.resourceManager().registerReloadListener(registry::reloadRenders);
    }

    /** Reloads the renders. This method is registered as a reload listener with Forge. */
    private void reloadRenders(IResourceManager manager) {

        Logger.info("Reloading resources...");
        //reload global animations
        try { animParser.parseGlobalFunctions(domains); }
        catch(AbieParseException e) {
            Logger.error("Exception parsing global functions; loading cannot continue.");
            Logger.error(e);
            return;
        }
        //reload models
        for(Map.Entry<ResourceLocation, ReloadableRender> entry : renders.entrySet()) {

            ResourceLocation estateLoc = entry.getKey();
            ReloadableRender render = entry.getValue();
            try(Reader rd = Util.getReaderFor(manager, estateLoc)) {

                Logger.info("Compiling models for " + estateLoc);
                EntityStateMap states = EntityStateMap.from(rd, render.getValidStates());
                render.reload(states);

            } catch(JsonParseException|IOException e) {

                Logger.error("Exception when parsing models for " + estateLoc);
                Logger.error(e);
                render.setMissingno();
            }
        }
        //reload custom dye colors
        for(CustomDyeColor colors : Enim.DYE_COLOR_REGISTRY.getValues())
            colors.reloadColors(manager);
        ModelCache.clearCache();
        Logger.info("Reload complete");
    }
}
