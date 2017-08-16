package kvverti.enim.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.renderer.tileentity.TileEntityBannerRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.Util;
import kvverti.enim.entity.state.RenderState;

public class BannerRender extends SignLikeRender<TileEntityBanner> {

	private static final String BANNER_PATTERNS = "minecraft:textures/entity/banner/";
	private final BannerTextures textureCache = new BannerTextures("b", BANNER_PATTERNS);

	public BannerRender() {

		super(Blocks.STANDING_BANNER);
	}

	@Override
	protected void preRender(TileEntityBanner tile, EntityInfo info) {

		super.preRender(tile, info);
		bindTexture(textureCache.getTexture(tile, getCurrentEntityState()));
	}

	@Override
	public RenderState getStateFromTile(TileEntityBanner tile) {

		//until we implement item rendering
		return tile.hasWorld() ? super.getStateFromTile(tile) : getStateManager().getDefaultState();
	}
}