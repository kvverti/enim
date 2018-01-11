package kvverti.enim.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.renderer.tileentity.TileEntityBannerRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.Util;
import kvverti.enim.entity.state.RenderState;
import kvverti.enim.model.EntityState;

public class BannerRender extends SignLikeRender<TileEntityBanner> {

    private static final String BANNER_PATTERNS = "minecraft:textures/entity/banner/";
    private final BannerTextures textureCache = new BannerTextures("b", BANNER_PATTERNS);

    public BannerRender() {

        super(Blocks.STANDING_BANNER);
    }

    @Override
    protected void preRenderLayer(TileEntityBanner tile, EntityInfo info, EntityState layer) {

        super.preRenderLayer(tile, info, layer);
        bindTexture(textureCache.getTexture(tile, layer));
    }

    @Override
    public RenderState getStateFromTile(TileEntityBanner tile) {

        //until we implement item rendering
        return tile.hasWorld() ? super.getStateFromTile(tile) : getStateManager().getDefaultState();
    }
}