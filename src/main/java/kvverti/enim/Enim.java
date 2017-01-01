package kvverti.enim;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.*;
import net.minecraft.tileentity.*;

import kvverti.enim.entity.*;

@Mod(modid = Enim.ID, name = Enim.NAME, version = Enim.VERSION, clientSideOnly = true)
public final class Enim {

	public static final String ID = "enim";
	public static final String NAME = "ENIM";
	public static final String VERSION = "dev-2016.12.31.0";
	//dev format: dev-year.month.day.edit
	//release format: major.minor.fix

	@Instance(ID)
	public static Enim instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {

	//	EnimRenderingRegistry.registerEntityRender(EntityBoat.class, m -> new BoatRender(m, "minecraft", "boat"));
		EnimRenderingRegistry.registerEntityRender(EntityLeashKnot.class, m -> new BasicRender<>(m, "minecraft", "lead"));
		EnimRenderingRegistry.registerEntityRender(EntityMinecartEmpty.class, m -> new MinecartRender(m, "minecraft", "minecart"));
		EnimRenderingRegistry.registerEntityRender(EntitySlime.class, m -> new SlimeRender(m, "minecraft", "slime"));
		EnimRenderingRegistry.registerEntityRender(EntityMagmaCube.class, m -> new SlimeRender(m, "minecraft", "magma_cube"));
		EnimRenderingRegistry.registerEntityRender(EntityCreeper.class, m -> new CreeperRender(m, "minecraft", "creeper"));
		EnimRenderingRegistry.registerEntityRender(EntityRabbit.class, m -> new RabbitRender(m, "minecraft", "rabbit"));
		EnimRenderingRegistry.registerEntityRender(EntityChicken.class, m -> new ChickenRender(m, "minecraft", "chicken"));
		EnimRenderingRegistry.registerEntityRender(EntityBlaze.class, m -> new BasicLivingRender<>(m, "minecraft", "blaze"));
		EnimRenderingRegistry.registerEntityRender(EntitySpider.class, m -> new BasicLivingRender<>(m, "minecraft", "spider"));
		EnimRenderingRegistry.registerEntityRender(EntityCaveSpider.class, m -> new BasicLivingRender<>(m, "minecraft", "cave_spider"));
		EnimRenderingRegistry.registerEntityRender(EntitySilverfish.class, m -> new BasicLivingRender<>(m, "minecraft", "silverfish"));
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {

		EnimRenderingRegistry.init(e);
		MinecraftForge.EVENT_BUS.register(Ticker.INSTANCE);
		EnimRenderingRegistry.registerTileEntityRender(TileEntitySign.class, new SignRender("minecraft", "sign"));
		EnimRenderingRegistry.registerTileEntityRender(TileEntityBanner.class, new BannerRender("minecraft", "banner"));
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {

	}
}