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

import kvverti.enim.Util;
import kvverti.enim.entity.state.RenderState;
import kvverti.enim.modelsystem.EntityState;

public class BannerRender extends SignLikeRender<TileEntityBanner> {

	private static final Method bindBannerTexture;
	private static final ResourceLocation bannerTextures;
	private static final Field bannerTextures_domain;
	private static final Field bannerTextures_path;
	private static final TileEntityBannerRenderer proxy = new TileEntityBannerRenderer();

	static {

		bindBannerTexture = Util.findMethod(TileEntityBannerRenderer.class,
			ResourceLocation.class,
			new String[] { "func_178463_a" },
			TileEntityBanner.class);

		Field textures =
			Util.findField(TileEntityBannerRenderer.class, ResourceLocation.class, "field_178464_d", "BANNERTEXTURES");
		bannerTextures = Util.getField(null, textures);

		bannerTextures_domain =
			Util.findField(ResourceLocation.class, String.class, "field_110626_a", "resourceDomain");

		bannerTextures_path =
			Util.findField(ResourceLocation.class, String.class, "field_110625_b", "resourcePath");
	}

	public BannerRender(String modDomain, String entityStateFile) {

		super(modDomain, entityStateFile, Blocks.standing_banner);
	}

	@Override
	public void preRender(TileEntityBanner tile, EntityState state, EntityInfo info) {

		setBannerTextures(state.textureFile());
		bindTexture(bindBannerTexture(tile));
	}

	@Override
	public RenderState getStateFromTile(TileEntityBanner tile) {

		//until we implement item rendering
		return tile.hasWorldObj() ? super.getStateFromTile(tile) : getStateManager().getDefaultState();
	}

	private ResourceLocation bindBannerTexture(TileEntityBanner banner) {

		ResourceLocation loc = Util.invokeUnchecked(proxy, bindBannerTexture, banner);
		return loc != null ? loc : Util.MISSING_LOCATION;
	}

	private void setBannerTextures(ResourceLocation loc) {

		Util.setField(bannerTextures, bannerTextures_domain, loc.getResourceDomain());
		Util.setField(bannerTextures, bannerTextures_path, loc.getResourcePath());
	}
}