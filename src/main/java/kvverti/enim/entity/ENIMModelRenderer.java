package kvverti.enim.entity;

import java.nio.FloatBuffer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import kvverti.enim.Vec3f;
import kvverti.enim.Util;
import kvverti.enim.model.ElementType;
import kvverti.enim.model.ModelElement;

import static kvverti.enim.entity.Entities.*;

public class ENIMModelRenderer extends ModelRenderer {

    private static final Method compileDisplayList;
    private static final Field displayList;

    private final Vec3f defaultScale;
    private final boolean translucent;
    private final boolean head;
    private final int tintIndex;
    private final float pivotDeltaX;
    private final float pivotDeltaY;
    private final float pivotDeltaZ;
    private final FloatBuffer initialAffineTransform = GLAllocation.createDirectFloatBuffer(16);
    private final ElementType type;
    private final ItemStack item;
    private final IBakedModel itemModel;
    private final IBlockState blockstate;
    private boolean compiled = false;
    public float rotateAngle;
    public float rotateAxisX;
    public float rotateAxisY;
    public float rotateAxisZ;
    public float shiftDistanceX;
    public float shiftDistanceY;
    public float shiftDistanceZ;
    // public final FloatBuffer affineTransform = FloatBuffer.allocate(16);

    static {

        compileDisplayList = Util.findMethod(ModelRenderer.class,
            void.class,
            "compileDisplayList",
            "func_78788_d",
            float.class);

        displayList = Util.findField(ModelRenderer.class, int.class, "field_78811_r", "displayList");
    }

    /* Make missingno */
    ENIMModelRenderer(ModelBase model) {

        super(model, "#missingno");
        defaultScale = Vec3f.IDENTITY;
        translucent = false;
        head = false;
        tintIndex = -1;
        pivotDeltaX = 0.0f;
        pivotDeltaY = 0.0f;
        pivotDeltaZ = 0.0f;
        // set to the identity matrix
        initialAffineTransform.put(0, 1.0f);
        initialAffineTransform.put(5, 1.0f);
        initialAffineTransform.put(10, 1.0f);
        initialAffineTransform.put(15, 1.0f);
        addBox(-8.0f, -16.0f, -8.0f, 16, 16, 16);
        type = ElementType.MODEL_BOX;
        item = null;
        itemModel = null;
        blockstate = null;
    }

    public ENIMModelRenderer(ModelBase model, ModelElement features) {

        super(model, features.name());
        defaultScale = features.scale();
        head = features.isHead();
        tintIndex = features.tintIndex();
        Vec3f origin = features.origin(), pivot = features.pivot(), from = features.from(), to = features.to();
        int[] uv = features.uv();
        setTextureOffset(uv[0], uv[1]);
        setRotationPoint(origin.x - 8.0f, -origin.y, 8.0f - origin.z);
        addBox(from.x - origin.x,
            origin.y - to.y,
            origin.z - to.z,
            (int) (to.x - from.x),
            (int) (to.y - from.y),
            (int) (to.z - from.z),
            features.isTextureMirrored());
        pivotDeltaX = pivot.x - origin.x;
        pivotDeltaY = origin.y - pivot.y;
        pivotDeltaZ = origin.z - pivot.z;
        // set up initial (fixed) affine transformation
        final float scale = 0.0625f;
        Matrix4f matrix = new Matrix4f();
        Vector3f vec = new Vector3f();
        vec.set(offsetX, offsetY, offsetZ);
        matrix.translate(vec);
        vec.set(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
        matrix.translate(vec);
        vec.set(pivotDeltaX * scale, pivotDeltaY * scale, pivotDeltaZ * scale);
        matrix.translate(vec);
        vec.set(0.0f, 0.0f, 1.0f);
        Vec3f defaultRotations = features.rotation();
        matrix.rotate(-toRadians(defaultRotations.z), vec);
        vec.set(0.0f, 1.0f, 0.0f);
        matrix.rotate(-toRadians(defaultRotations.y), vec);
        vec.set(1.0f, 0.0f, 0.0f);
        matrix.rotate(+toRadians(defaultRotations.x), vec);
        matrix.store(initialAffineTransform);
        initialAffineTransform.rewind();
        // set up type specific render info
        type = features.type();
        switch(type) {
            case ITEM:
                item = new ItemStack(features.item());
                itemModel = renderItem.getItemModelWithOverrides(item, null, null);
                blockstate = null;
                translucent = true;
                break;
            case BLOCK:
                item = new ItemStack(features.blockstate().getBlock());
                itemModel = Minecraft.getMinecraft()
                    .getBlockRendererDispatcher()
                    .getModelForState(features.blockstate());
                blockstate = features.blockstate();
                translucent = features.blockstate()
                    .getBlock()
                    .getBlockLayer() == BlockRenderLayer.TRANSLUCENT;
                break;
            default:
                item = null;
                itemModel = null;
                blockstate = null;
                translucent = features.isTranslucent();
        }
    }

    public void transformWithoutRendering(EntityInfo info) {

        moveIntoPosition(info);
        fixTransformsForChildren();
    }

    public void render(EntityInfo info) {

        if(!isHidden && showModel) {

            if(!compiled) compileDisplayList();
            GlStateManager.pushMatrix();
            //render
            moveIntoPosition(info);
            Vec3f color = info.color.apply(tintIndex);
            if(translucent || info.alpha < 1.0f) makeLucent();
            GlStateManager.color(color.x, color.y, color.z, info.alpha);
            if(type == ElementType.MODEL_BOX)
                GlStateManager.callList(displayList());
            else {
                ResourceLocation tex = Entities.getCurrentTexture();
                Entities.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                renderItemModel();
                Entities.bindTexture(tex);
            }
            if(translucent || info.alpha < 1.0f) endLucent();
            //render children
            fixTransformsForChildren();
            if(childModels != null) childModels.forEach(box -> ((ENIMModelRenderer) box).render(info));
            GlStateManager.popMatrix();
        }
    }

    private void moveIntoPosition(EntityInfo info) {

        final float scale = 0.0625f;
        //transform element into position
        GlStateManager.multMatrix(initialAffineTransform);
        //apply special transformations
        if(head) {

            GlStateManager.rotate(info.headYaw, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(info.entityPitch, 1.0f, 0.0f, 0.0f);
        }
        //apply animations
        GlStateManager.translate(shiftDistanceX * scale, -shiftDistanceY * scale, -shiftDistanceZ * scale);
        GlStateManager.rotate(toDegrees(rotateAngle), rotateAxisX, rotateAxisY, rotateAxisZ);
        GlStateManager.translate(-pivotDeltaX * scale, -pivotDeltaY * scale, -pivotDeltaZ * scale);
        GlStateManager.scale(defaultScale.x, defaultScale.y, defaultScale.z);
    }

    private void fixTransformsForChildren() {

        final float scale = 0.0625f;
        //do some transformations so children render properly
        GlStateManager.translate(-rotationPointX * scale, -rotationPointY * scale, -rotationPointZ * scale);
        GlStateManager.translate(-offsetX, -offsetY, -offsetZ);
    }

    @Override
    @Deprecated
    public void render(float scale) {

        EntityInfo info = new EntityInfo();
        info.scale = scale;
        render(info);
    }

    @Override
    @Deprecated
    public void renderWithRotation(float scale) {

        throw new UnsupportedOperationException("ENIMModelRenderer");
    }

    private void makeLucent() {

        GlStateManager.enableNormalize();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
    }

    private void endLucent() {

        GlStateManager.disableBlend();
        GlStateManager.disableNormalize();
    }

    private void compileDisplayList() {

        Util.invokeUnchecked(this, compileDisplayList, 0.0625f);
        compiled = true;
    }

    private int displayList() {

        return Util.getIntField(this, displayList);
    }

    private static final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

    private static final Method renderQuads = Util.findMethod(RenderItem.class,
        void.class,
        "renderQuads",
        "func_191970_a",
        BufferBuilder.class,
        List.class,
        int.class,
        ItemStack.class);

    /** Modified from RenderItem#renderModel */
    private void renderItemModel() {

        GlStateManager.pushMatrix();
        GlStateManager.enableCull();
        GlStateManager.rotate(-180.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.translate(-0.5f, -0.5f, -0.5f);
        if(itemModel.isBuiltInRenderer()) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableRescaleNormal();
            TileEntityItemStackRenderer.instance.renderByItem(item);
        } else {
            Tessellator tez = Tessellator.getInstance();
            BufferBuilder buffer = tez.getBuffer();
            buffer.begin(7, DefaultVertexFormats.ITEM);
            for(EnumFacing side : EnumFacing.values()) {
                Util.invokeUnchecked(renderItem,
                    renderQuads,
                    buffer,
                    itemModel.getQuads(blockstate, side, 0L),
                    -1,
                    item);
            }
            Util.invokeUnchecked(renderItem,
                renderQuads,
                buffer,
                itemModel.getQuads(blockstate, null, 0L),
                -1,
                item);
            tez.draw();
        }
        GlStateManager.disableCull();
        GlStateManager.popMatrix();
    }
}
