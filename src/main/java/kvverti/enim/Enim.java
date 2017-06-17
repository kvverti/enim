package kvverti.enim;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.RegistryBuilder;

import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.*;
import net.minecraft.tileentity.*;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.entity.*;
import kvverti.enim.entity.animation.AnimType;
import kvverti.enim.entity.animation.EntityFrameTimers;
import kvverti.enim.entity.color.CustomDyeColor;

import static kvverti.enim.EnimRenderingRegistry.registerEntityRender;
import static kvverti.enim.EnimRenderingRegistry.registerTileEntityRender;
import static kvverti.enim.entity.animation.MinecraftAnimTypes.*;
import static kvverti.enim.entity.color.MinecraftCustomDyeColors.*;

@EventBusSubscriber
@Mod(modid = Enim.ID, name = Enim.NAME, version = Enim.VERSION, acceptedMinecraftVersions = "[1.11,1.12)", clientSideOnly = true)
public final class Enim {

	public static final String ID = "enim";
	public static final String NAME = "ENIM";
	public static final String VERSION = "indev";
	//version will stay indev until releases start coming

	static {
		//the null armor material
		EnumHelper.addArmorMaterial("ENIM_NONE", "none", 0, new int[4], 0, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0f);
	}

	public static final IForgeRegistry<AnimType> ANIM_TYPE_REGISTRY = new RegistryBuilder<AnimType>()
		.setName(new ResourceLocation(ID, "animations"))
		.setType(AnimType.class)
		.setIDRange(0, 4095)
		.create();

	public static final IForgeRegistry<CustomDyeColor> DYE_COLOR_REGISTRY = new RegistryBuilder<CustomDyeColor>()
		.setName(new ResourceLocation(ID, "colors"))
		.setType(CustomDyeColor.class)
		.setIDRange(0, 255)
		.create();

	@SubscribeEvent
	public static void registerAnimTypes(RegistryEvent.Register<AnimType> event) {

		Logger.info("Registering Minecraft AnimTypes...");
		event.getRegistry().registerAll(IDLE, MOVE, AIR, SWIM, TRACK, JUMP, EAT, OPEN, CLOSE);
	}

	@SubscribeEvent
	public static void registerCustomDyeColors(RegistryEvent.Register<CustomDyeColor> event) {

		Logger.info("Registering Minecraft CustomDyeColors...");
		event.getRegistry().register(DEFAULT);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {

		Logger.info("Registering Minecraft entity renders...");
	//	registerEntityRender(EntityBoat.class, m -> new BoatRender(m, "minecraft", "boat"));
		registerEntityRender(EntityLeashKnot.class, "minecraft", "lead", BasicRender::new);
		registerEntityRender(EntityMinecartEmpty.class, "minecraft", "minecart", MinecartRender::new);
		registerEntityRender(EntitySlime.class, "minecraft", "slime", SlimeRender::new);
		registerEntityRender(EntityMagmaCube.class, "minecraft", "magma_cube", SlimeRender::new);
		registerEntityRender(EntityCreeper.class, "minecraft", "creeper", CreeperRender::new);
		registerEntityRender(EntityRabbit.class, "minecraft", "rabbit", RabbitRender::new);
		registerEntityRender(EntityChicken.class, "minecraft", "chicken", GrowableAnimalRender::new);
		registerEntityRender(EntityBlaze.class, "minecraft", "blaze", BasicLivingRender::new);
		registerEntityRender(EntitySpider.class, "minecraft", "spider", BasicLivingRender::new);
		registerEntityRender(EntityCaveSpider.class, "minecraft", "cave_spider", BasicLivingRender::new);
		registerEntityRender(EntitySilverfish.class, "minecraft", "silverfish", BasicLivingRender::new);
		registerEntityRender(EntityEndermite.class, "minecraft", "endermite", BasicLivingRender::new);
		registerEntityRender(EntityBat.class, "minecraft", "bat", BasicLivingRender::new);
		registerEntityRender(EntitySheep.class, "minecraft", "sheep", SheepRender::new);
		registerEntityRender(EntityPig.class, "minecraft", "pig", PigRender::new);
		registerEntityRender(EntityCow.class, "minecraft", "cow", GrowableAnimalRender::new);
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {

		Logger.info("Registering Minecraft tile entity renders...");
		registerTileEntityRender(TileEntitySign.class, "minecraft", "sign", new SignRender());
		registerTileEntityRender(TileEntityBanner.class, "minecraft", "banner", new BannerRender());
		registerTileEntityRender(TileEntityChest.class, "minecraft", "chest", new ChestRender());
		registerTileEntityRender(TileEntityEnderChest.class, "minecraft", "ender_chest", new EnderChestRender());
		EnimRenderingRegistry.init(e);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {

	}

	@EventHandler
	public void serverStopped(FMLServerStoppedEvent e) {

		EntityFrameTimers.clearAll();
	}
}