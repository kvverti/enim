package kvverti.enim.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.Vec3f;
import kvverti.enim.Util;
import kvverti.enim.model.ElementType;
import kvverti.enim.model.ModelElement;
import kvverti.enim.model.ModelProperties;

import static kvverti.enim.entity.Entities.*;

public class ENIMModelRenderer extends ModelRenderer {

    private static final Method compileDisplayList;
    private static final Field displayList;

    private final Vec3f defaultRotations;
    private final Vec3f defaultScale;
    private final boolean translucent;
    private final boolean head;
    private final int tintIndex;
    private final float pivotDeltaX;
    private final float pivotDeltaY;
    private final float pivotDeltaZ;
    private final ElementType type;
    private final ItemStack item;
    private final IBakedModel itemModel;
    private boolean compiled = false;
    public float shiftDistanceX;
    public float shiftDistanceY;
    public float shiftDistanceZ;

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
        defaultRotations = Vec3f.ORIGIN;
        defaultScale = Vec3f.IDENTITY;
        translucent = false;
        head = false;
        tintIndex = -1;
        pivotDeltaX = 0.0f;
        pivotDeltaY = 0.0f;
        pivotDeltaZ = 0.0f;
        addBox(-8.0f, -16.0f, -8.0f, 16, 16, 16);
        type = ElementType.MODEL_BOX;
        item = null;
        itemModel = null;
    }

    public ENIMModelRenderer(ModelBase model, ModelElement features) {

        super(model, features.name());
        defaultRotations = features.rotation();
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
        type = features.type();
        switch(type) {
            case ITEM:
                item = new ItemStack(features.item());
                itemModel = null;
                translucent = true;
                break;
            case BLOCK:
                item = new ItemStack(features.blockstate().getBlock());
                itemModel = Minecraft.getMinecraft()
                    .getBlockRendererDispatcher()
                    .getModelForState(features.blockstate());
                translucent = features.blockstate()
                    .getBlock()
                    .getBlockLayer() == BlockRenderLayer.TRANSLUCENT;
                break;
            default:
                item = null;
                itemModel = null;
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
                if(type == ElementType.ITEM) {
                    Minecraft.getMinecraft()
                        .getRenderItem()
                        .renderItem(item, TransformType.FIXED);
                } else {
                    //TODO: some blocks (multipart models?) don't render
                    Minecraft.getMinecraft()
                        .getRenderItem()
                        .renderItem(item, itemModel);
                }
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
        GlStateManager.translate(offsetX, offsetY, offsetZ);
        GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
        GlStateManager.translate(pivotDeltaX * scale, pivotDeltaY * scale, pivotDeltaZ * scale);
        GlStateManager.rotate(-defaultRotations.z, 0.0f, 0.0f, 1.0f);
        GlStateManager.rotate(-defaultRotations.y, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(+defaultRotations.x, 1.0f, 0.0f, 0.0f);
        //apply special transformations
        if(head) {

            GlStateManager.rotate(info.headYaw, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(info.entityPitch, 1.0f, 0.0f, 0.0f);
        }
        //apply animations
        GlStateManager.translate(shiftDistanceX * scale, -shiftDistanceY * scale, -shiftDistanceZ * scale);
        GlStateManager.rotate(-toDegrees(rotateAngleZ), 0.0f, 0.0f, 1.0f);
        GlStateManager.rotate(-toDegrees(rotateAngleY), 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(+toDegrees(rotateAngleX), 1.0f, 0.0f, 0.0f);
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

        if(!isHidden && showModel) {

            if(!compiled) compileDisplayList();
            GlStateManager.pushMatrix();
            GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
            GlStateManager.rotate(+defaultRotations.z, 0.0f, 0.0f, 1.0f);
            GlStateManager.rotate(+defaultRotations.y, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(-defaultRotations.x, 1.0f, 0.0f, 0.0f);
            GlStateManager.rotate(+toDegrees(rotateAngleZ), 0.0f, 0.0f, 1.0f);
            GlStateManager.rotate(+toDegrees(rotateAngleY), 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(-toDegrees(rotateAngleX), 1.0f, 0.0f, 0.0f);
            GlStateManager.callList(displayList());
            GlStateManager.popMatrix();
        }
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
}