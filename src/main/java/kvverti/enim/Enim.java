package kvverti.enim;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import net.minecraft.entity.item.EntityBoat;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.entity.*;
import kvverti.enim.modelsystem.*;

import static kvverti.enim.Meta.*;

@Mod(modid = ID, name = NAME, version = VERSION, clientSideOnly = true)
public final class Enim implements IResourceManagerReloadListener {

	@Instance(ID)
	public static Enim instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {

	}

	@EventHandler
	public void init(FMLInitializationEvent e) {

		IReloadableResourceManager manager =
			(IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
    		manager.registerReloadListener(Enim.instance);

		RenderingRegistry.registerEntityRenderingHandler(
			EntityBoat.class, new ENIMRender<EntityBoat>("minecraft", "boat", new ENIMModel()));
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {

	}

	@Override
	public void onResourceManagerReload(IResourceManager manager) {

		Logger.info("Reloading resources...");

		List<Texture> tex = new ArrayList<>();
		Map<String, ResourceLocation> models = new HashMap<>();
		Set<ModelElement> elems = new HashSet<>();
		for(ENIMRender<?> r : ENIMRender.renders) {

			try {
				ResourceLocation estateLoc = r.getEntityStateFile();
				EntityJsonParser parser = new EntityJsonParser(manager.getResource(estateLoc));
				parser.parseTextures(tex);
				parser.parseModelLocations(models);

				EntityJsonParser mpsr = new EntityJsonParser(
					manager.getResource(models.get(Keys.STATE_NORMAL)));
				mpsr.parseElements(elems);
				mpsr.getElementImports(elems);
				r.reloadRender(elems, tex);

			} catch(ENIMException|IOException e) {

				Logger.error(e);
				r.setMissingno();

			} finally {

				tex.clear();
				models.clear();
				elems.clear();
			}
		}

		Logger.info("Reload complete");
	}
}