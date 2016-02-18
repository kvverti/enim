package kvverti.enim;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import net.minecraftforge.fml.client.registry.ClientRegistry;
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
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.entity.*;
import kvverti.enim.modelsystem.*;

@Mod(modid = Enim.ID, name = Enim.NAME, version = Enim.VERSION, clientSideOnly = true)
public final class Enim implements IResourceManagerReloadListener {

	public static final String ID = "enim";
	public static final String NAME = "ENIM";
	public static final String VERSION = "dev-2016.02.16";

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
			EntityBoat.class, new ENIMRender<EntityBoat>("minecraft", "boat"));
		RenderingRegistry.registerEntityRenderingHandler(
			EntityLeashKnot.class, new ENIMRender<EntityLeashKnot>("minecraft", "lead"));

		ClientRegistry.bindTileEntitySpecialRenderer(
			TileEntitySign.class, new SignRender("minecraft", "sign"));
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {

	}

	@Override
	public void onResourceManagerReload(IResourceManager manager) {

		Logger.info("Reloading resources...");

		Map<String, EntityState> models = new HashMap<>();
		for(ReloadableRender r : ReloadableRender.renders) {

			try {
				ResourceLocation estateLoc = r.getEntityStateFile();
				EntityJsonParser parser = new EntityJsonParser(manager.getResource(estateLoc));
				parser.parseModelLocations(r.getEntityStateNames(), models);

				for(EntityState state : models.values()) {

					r.reloadRender(state);
				}

			} catch(ENIMException|IOException e) {

				Logger.error(e);
				r.setMissingno();

			} finally {

				models.clear();
			}
		}

		Logger.info("Reload complete");
	}
}