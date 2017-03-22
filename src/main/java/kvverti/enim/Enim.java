package kvverti.enim;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.RegistryBuilder;

import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.entity.*;
import kvverti.enim.entity.animation.AnimType;

import static kvverti.enim.EnimRenderingRegistry.registerEntityRender;
import static kvverti.enim.EnimRenderingRegistry.registerTileEntityRender;
import static kvverti.enim.entity.animation.MinecraftAnimTypes.*;

@Mod(modid = Enim.ID, name = Enim.NAME, version = Enim.VERSION, acceptedMinecraftVersions = "[1.11,1.12)", clientSideOnly = true)
public final class Enim {

	public static final String ID = "enim";
	public static final String NAME = "ENIM";
	public static final String VERSION = "dev-2016.12.31.0";
	//dev format: dev-year.month.day.edit
	//release format: major.minor.fix

	@Instance(ID)
	public static Enim instance;

	public static final IForgeRegistry<AnimType> ANIM_TYPE_REGISTRY = new RegistryBuilder<AnimType>()
		.setName(new ResourceLocation(ID, "animations"))
		.setType(AnimType.class)
		.setIDRange(0, 128)
		.create();

	static {
		//register minecraft animation types
		ANIM_TYPE_REGISTRY.registerAll(IDLE, MOVE, AIR, SWIM, TRACK, JUMP);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {

	//	registerEntityRender(EntityBoat.class, m -> new BoatRender(m, "minecraft", "boat"));
		registerEntityRender(EntityLeashKnot.class, "minecraft", "lead", BasicRender::new);
		registerEntityRender(EntityMinecartEmpty.class, "minecraft", "minecart", MinecartRender::new);
		registerEntityRender(EntitySlime.class, "minecraft", "slime", SlimeRender::new);
		registerEntityRender(EntityMagmaCube.class, "minecraft", "magma_cube", SlimeRender::new);
		registerEntityRender(EntityCreeper.class, "minecraft", "creeper", CreeperRender::new);
		registerEntityRender(EntityRabbit.class, "minecraft", "rabbit", RabbitRender::new);
		registerEntityRender(EntityChicken.class, "minecraft", "chicken", ChickenRender::new);
		registerEntityRender(EntityBlaze.class, "minecraft", "blaze", BasicLivingRender::new);
		registerEntityRender(EntitySpider.class, "minecraft", "spider", BasicLivingRender::new);
		registerEntityRender(EntityCaveSpider.class, "minecraft", "cave_spider", BasicLivingRender::new);
		registerEntityRender(EntitySilverfish.class, "minecraft", "silverfish", BasicLivingRender::new);
		registerEntityRender(EntityBat.class, "minecraft", "bat", BasicLivingRender::new);
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {

		EnimRenderingRegistry.init(e);
		MinecraftForge.EVENT_BUS.register(Ticker.INSTANCE);
		registerTileEntityRender(TileEntitySign.class, "minecraft", "sign", new SignRender());
		registerTileEntityRender(TileEntityBanner.class, "minecraft", "banner", new BannerRender());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {

	}
}