package kvverti.enim.entity;

import java.util.List;

import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableList;

import kvverti.enim.entity.state.RenderState;
import kvverti.enim.entity.state.StateManager;
import kvverti.enim.model.EntityState;
import kvverti.enim.model.EntityStateMap;
import kvverti.enim.Keys;
import kvverti.enim.Vec3f;

/** Base class for ENIM reloadable tile entity renders. */
public abstract class ENIMTileEntityRender<T extends TileEntity> extends TileEntitySpecialRenderer<T> implements ReloadableRender {

    private final StateManager stateManager;
    private EntityState currentState;

    protected ENIMTileEntityRender(IProperty<?>... properties) {

        this.stateManager = new StateManager(properties);
    }

    public abstract RenderState getStateFromTile(T tile);

    protected final StateManager getStateManager() {

        return stateManager;
    }

    protected final EntityState getCurrentEntityState() {

        return currentState;
    }

    @Override
    public final void render(T tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        Entities.bindTexture(null); //unset texture
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5f, (float) y, (float) z + 0.5f);
        GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);

        RenderState renderState = getStateFromTile(tileEntity);
        ImmutableList<EntityState> entityStates = stateManager.getStateLayers(renderState);
        currentState = entityStates.get(0);
        EntityInfo info = new EntityInfo();
        info.partialTicks = partialTicks;
        info.color = i -> i < 0 ? getBaseColor(tileEntity, info) : getBaseColor(tileEntity, info).scale(getColorOverlay(tileEntity, info, i));
        GEntity e = new GEntity(tileEntity);
        preRender(tileEntity, info);
        boolean render = shouldRender(tileEntity);
        for(EntityState layer : entityStates)
            renderLayer(e, info, layer, render);
        postRender(tileEntity, info);
        GlStateManager.popMatrix();
    }

    @SuppressWarnings("unchecked")
    private void renderLayer(GEntity tile, EntityInfo info, EntityState layer, boolean render) {

        GlStateManager.pushMatrix();
        bindTexture(layer.texture());
        info.scale = layer.scale();
        GlStateManager.rotate(layer.y(), 0.0f, 1.0f, 0.0f);
        GlStateManager.scale(info.scale, info.scale, info.scale);
        ENIMModel model = stateManager.getModel(layer);
        preRenderLayer((T) tile.getTileEntity(), info, layer);
        if(render)
            model.render(tile, info);
        ResourceLocation overlay = currentState.overlay();
        if(overlay != null)
            renderOverlay(tile, info, model, overlay);
        postRenderLayer((T) tile.getTileEntity(), info, layer);
        GlStateManager.popMatrix();
    }

    private void renderOverlay(GEntity tile, EntityInfo info, ENIMModel model, ResourceLocation overlay) {

        bindTexture(overlay);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(1, 1);
        //begin magic
        GlStateManager.depthMask(true);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0xf0f0, 0.0f);
        //end magic
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        model.render(tile, info);
        //begin magic
        net.minecraft.util.math.BlockPos pos = tile.getTileEntity().getPos();
        net.minecraft.world.World world = tile.getTileEntity().getWorld();
        int brightness = world != null && world.isBlockLoaded(pos) ?
            world.getCombinedLight(pos, 0)
            : 0;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightness % 0x10000, brightness / 0x10000);
        //end magic
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    public boolean shouldRender(T tile) { return true; }

    /**
     * Method called immediately before the main model is rendered.
     */
    protected void preRender(T tile, EntityInfo info) { }
    
    /**
     * Method called immediately before a layer is rendered.
     */
    protected void preRenderLayer(T tile, EntityInfo info, EntityState layer) { }
    
    /**
     * Method called immediately after a layer is rendered.
     */
    protected void postRenderLayer(T tile, EntityInfo info, EntityState layer) { }

    /**
     * Method called immediately after the main model and any layers are rendered.
     */
    protected void postRender(T tile, EntityInfo info) { }

    /** Returns the color in RGB format that will be multiplied onto all model elements, regardless of tintindex. */
    public Vec3f getBaseColor(T tile, EntityInfo info) {

        return Vec3f.IDENTITY;
    }

    /**
     * Returns the color in RGB format that will be overlayed (multiplied) onto applicable model elements.
     */
    public Vec3f getColorOverlay(T tile, EntityInfo info, int colorIndex) {

        return Vec3f.IDENTITY;
    }

    @Override
    public final ImmutableList<RenderState> getValidStates() {

        return stateManager.getRenderStates();
    }

    @Override
    public final void reload(EntityStateMap states) {

        stateManager.reloadStates(states);
    }

    @Override
    public final void setMissingno() {

        stateManager.setAllInvalid();
    }
    
    @Override
    protected void bindTexture(ResourceLocation texture) {
        
        Entities.bindTexture(texture);
    }
}