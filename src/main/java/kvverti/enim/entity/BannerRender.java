package kvverti.enim.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityBannerRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

import kvverti.enim.Util;
import kvverti.enim.modelsystem.EntityState;

public class BannerRender extends SignLikeRender<TileEntityBanner> {

	private static final Method bindBannerTexture;
	private static final ResourceLocation bannerTextures;
	private static final Field bannerTextures_domain;
	private static final Field bannerTextures_path;
	private static final TileEntityBannerRenderer proxy = new TileEntityBannerRenderer();

	static {

		bindBannerTexture = ReflectionHelper.findMethod(
			TileEntityBannerRenderer.class,
			null,
			new String[] { "func_178463_a" },
			TileEntityBanner.class);
		assert bindBannerTexture.getReturnType() == ResourceLocation.class : "Type of bindBannerTexture()";

		Field textures = ReflectionHelper.findField(TileEntityBannerRenderer.class, "BANNERTEXTURES", "field_178464_d");
		assert textures.getType() == ResourceLocation.class : "Type of bannerTextures";
		bannerTextures = Util.getField(textures, null);

		bannerTextures_domain = ReflectionHelper.findField(ResourceLocation.class, "resourceDomain", "field_110626_a");
		assert bannerTextures_domain.getType() == String.class : "Type of resourceDomain";

		bannerTextures_path = ReflectionHelper.findField(ResourceLocation.class, "resourcePath", "field_110625_b");
		assert bannerTextures_path.getType() == String.class : "Type of resourcePath";
	}

	public BannerRender(String modDomain, String entityStateFile) {

		super(modDomain, entityStateFile, Blocks.standing_banner);
	}

	@Override
	public void preRender(TileEntityBanner tile, EntityState state, double x, double y, double z, float partialTicks, int destroyStage) {

		setBannerTextures(state.textureFile());
		bindTexture(bindBannerTexture(tile));
	}

	@Override
	public EntityState getStateFromTile(TileEntityBanner tile) {

		if(tile.getWorld() == null) return states.get("floor_00"); //until we implement item rendering
		return super.getStateFromTile(tile);
	}

	private ResourceLocation bindBannerTexture(TileEntityBanner banner) {

		return Util.invokeUnchecked(bindBannerTexture, proxy, banner);
	}

	private void setBannerTextures(ResourceLocation loc) {

		Util.setField(bannerTextures_domain, bannerTextures, loc.getResourceDomain());
		Util.setField(bannerTextures_path, bannerTextures, loc.getResourcePath());
	}
}