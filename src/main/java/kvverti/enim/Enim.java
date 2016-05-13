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
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.*;
import net.minecraft.entity.item.*;
import net.minecraft.tileentity.*;
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

		registerEntity(EntityBoat.class, m -> new ENIMRender<>(m, "minecraft", "boat"));
		registerEntity(EntityLeashKnot.class, m -> new ENIMRender<>(m, "minecraft", "lead"));
		registerEntity(EntityMinecartEmpty.class, m -> new MinecartRender(m, "minecraft", "minecart"));
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {

    		Entities.resourceManager().registerReloadListener(Enim.instance);

		registerTile(TileEntitySign.class, new SignRender("minecraft", "sign"));
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
				models.values().forEach(r::reloadRender);

			} catch(ENIMException|IOException e) {

				Logger.error(e);
				r.setMissingno();

			} finally {

				models.clear();
			}
		}

		Logger.info("Reload complete");
	}

	private <T extends Entity> void registerEntity(Class<T> cls, IRenderFactory<? super T> factory) {

		RenderingRegistry.registerEntityRenderingHandler(cls, factory);
	}

	private <T extends TileEntity> void registerTile(Class<T> cls, ENIMTileEntityRender<? super T> render) {

		ClientRegistry.bindTileEntitySpecialRenderer(cls, render);
	}
}