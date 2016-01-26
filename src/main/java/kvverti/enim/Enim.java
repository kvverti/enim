package kvverti.enim;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

import net.minecraft.entity.item.EntityMinecart;

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
			EntityMinecart.class, new ENIMRender<EntityMinecart>(new ENIMModel()));
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {

	}

	@Override
	public void onResourceManagerReload(IResourceManager manager) {

		Logger.info("Reloading resources...");

		try {
			EntityJsonParser parser = new EntityJsonParser(
				manager.getResource(new ResourceLocation(ID, "models/entity/entityJson.json")));

			Set<ModelElement> elems = new HashSet<>();
			parser.parseElements(elems);
			parser.getElementImports(elems);

			for(ENIMRender r : ENIMRender.renders) {

				r.reloadRender(elems);
			}

		} catch(ENIMException|IOException e) {

			Logger.error(e);
		}

		Logger.info("Reload complete");
	}
}