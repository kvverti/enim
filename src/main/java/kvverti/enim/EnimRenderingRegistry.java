package kvverti.enim;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
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

import kvverti.enim.entity.ReloadableRender;
import kvverti.enim.entity.Entities;
import kvverti.enim.modelsystem.EntityJsonParser;
import kvverti.enim.modelsystem.EntityState;
import kvverti.enim.modelsystem.ENIMException;

/**
 * Registry for ENIM related rendering features. This includes all entity and tile entity renders as well as armor items
 * and anything else that ENIM must be made aware of.
 */
public final class EnimRenderingRegistry {

	/** The singleton instance */
	private static final EnimRenderingRegistry registry = new EnimRenderingRegistry();

	/** Stores all registered renders so they can be reloaded */
	private final Collection<ReloadableRender> renders = new HashSet<>(20);

	/** Construction disallowed */
	private EnimRenderingRegistry() { }

	/**
	 * Registers a reloadable entity renderer. Renders registered through this method will be reloaded with the game resources
	 * when resource packs change, in addition to being registered with Forge. This method should be called during the
	 * preinitialization phase.
	 * @param <T> The type of entity
	 * @param <R> The type of renderer
	 * @param cls The entity class which will be rendered
	 * @param factory A factory that returns a reloadable entity render with the given RenderManager
	 */
	public static <T extends Entity, R extends Render<? super T> & ReloadableRender>
		void registerEntityRender(Class<T> cls, Function<? super RenderManager, ? extends R> factory) {

		RenderingRegistry.registerEntityRenderingHandler(cls, manager -> {

			R r = factory.apply(manager);
			registry.renders.add(r);
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
		void registerTileEntityRender(Class<T> cls, R render) {

		ClientRegistry.bindTileEntitySpecialRenderer(cls, render);
		registry.renders.add(render);
	}

	/** EventHandler - called during init phase */
	static void init(FMLInitializationEvent e) {

    		Entities.resourceManager().registerReloadListener(registry::reloadRenders);
	}

	/** Reloads the renders. This method is registered as a reload listener with Forge. */
	private void reloadRenders(IResourceManager manager) {

		Logger.info("Reloading resources...");
		Map<String, EntityState> models = new HashMap<>();
		for(ReloadableRender r : renders) {

			try {
				ResourceLocation estateLoc = r.getEntityStateFile();
				EntityJsonParser parser = new EntityJsonParser(manager.getResource(estateLoc));
				parser.parseModelLocations(r.getEntityStateNames(), models);
				models.values().forEach(r::reloadRender);

			} catch(ENIMException|IOException e) {

				Logger.error(e);
				r.setMissingno();

			} finally {

				models.clear();
			}
		}
		try { kvverti.enim.model.EntityModel model = kvverti.enim.model.EntityModel.GSON.fromJson(Util.getReaderFor(new ResourceLocation("minecraft:models/entity/rabbit.json")), kvverti.enim.model.EntityModel.class);
			Logger.info(model); }
		catch(Exception e) { Logger.error(e); }
		Logger.info("Reload complete");
	}
}