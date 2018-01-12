package kvverti.enim.entity;

import java.util.function.IntFunction;

import net.minecraft.block.properties.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.text.TextFormatting;

import com.google.common.collect.ImmutableList;

import kvverti.enim.Vec3f;
import kvverti.enim.model.ModelProperties;
import kvverti.enim.model.ArmorModel;
import kvverti.enim.model.EntityState;

/** Base class for ENIM reloadable living entity (mob) renders. */
public abstract class LivingRender<T extends EntityLivingBase> extends ENIMRender<T> {

    public static final float NAMETAG_VISIBILITY_RANGE_SQ = 64.0f * 64.0f;
    public static final IProperty<Boolean> BABY = PropertyBool.create("baby");

    protected LivingRender(RenderManager manager, IProperty<?>... properties) {

        super(manager, properties);
    }

    @Override
    public boolean shouldRender(T entity) {

        return !entity.isInvisible() || !entity.isInvisibleToPlayer(Entities.thePlayer());
    }

    @Override
    public Vec3f getBaseColor(T entity, EntityInfo info) {

        //tint red when damaged
        if(entity.hurtTime > 0 || entity.deathTime > 0)
            return Vec3f.of(1.0f, 0.5f, 0.5f);
        return Vec3f.IDENTITY;
    }

    @Override
    public float getBaseAlpha(T entity, EntityInfo info) {

        //invisible mobs as translucent to players in creative/spectator
        if(entity.isInvisible() && !entity.isInvisibleToPlayer(Entities.thePlayer()))
            return 0.25f;
        return 1.0f;
    }

    /* Must call super.preRender(entity, state, info); in subclasses!! */
    @Override
    protected void preRender(T entity, EntityInfo info) {

        super.preRender(entity, info);
        //fall over when dead
        if(entity.deathTime > 0)
            rotateCorpse(entity);
        //"Dinnerbone" or "Grumm" mobs render upside down
        if(entity.hasCustomName()) {

            String name = TextFormatting.getTextWithoutFormattingCodes(entity.getName());
            if("Grumm".equals(name) || "Dinnerbone".equals(name)) {

                GlStateManager.translate(0.0f, -entity.height, 0.0f);
                GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
            }
        }
    }

    @Override
    protected void postRender(T entity, EntityInfo info) {

        //render armor
        ArmorModel armor = getCurrentEntityState().armor();
        if(armor != null) {
            GEntity e = new GEntity(entity);
            IntFunction<Vec3f> oldColor = info.color;
            for(EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                if(slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                    
                    ArmorMaterial material = getMaterial(entity, slot);
                    if(material != null) {
                        
                        ImmutableList<EntityState> layers = armor.getArmorLayers(material, slot);
                        info.color = i -> i == 2 ? getArmorColor(entity, slot) : oldColor.apply(i);
                        for(EntityState armorState : layers)
                            renderLayer(e, info, armorState, true);
                    }
                }
            }
        }
        //render held/worn items
        boolean leftHanded = entity.getPrimaryHand() == EnumHandSide.LEFT;
        ItemStack right, left, head;
        right = leftHanded ? entity.getHeldItemOffhand() : entity.getHeldItemMainhand();
        left = leftHanded ? entity.getHeldItemMainhand() : entity.getHeldItemOffhand();
        head = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        ModelProperties properties = getCurrentEntityState().model().properties();
        renderItem(entity, info, right, TransformType.THIRD_PERSON_RIGHT_HAND, properties.rightHand());
        renderItem(entity, info, left, TransformType.THIRD_PERSON_LEFT_HAND, properties.leftHand());
        if(!(head.getItem() instanceof ItemArmor) //prevent armor rendering as an item
            || ((ItemArmor) head.getItem()).getEquipmentSlot() != EntityEquipmentSlot.HEAD
            || armor == null)
            renderItem(entity, info, head, TransformType.HEAD, properties.helmet());
        super.postRender(entity, info);
    }

    private ArmorMaterial getMaterial(T entity, EntityEquipmentSlot slot) {

        ItemStack stack = entity.getItemStackFromSlot(slot);
        if(!stack.isEmpty() && stack.getItem() instanceof ItemArmor) {

            ItemArmor armor = (ItemArmor) stack.getItem();
            if(armor.getEquipmentSlot() == slot)
                return armor.getArmorMaterial();
        }
        return null;
    }
    
    private Vec3f getArmorColor(T entity, EntityEquipmentSlot slot) {
        
        ItemStack item = entity.getItemStackFromSlot(slot);
        int color = ((ItemArmor) item.getItem()).getColor(item);
        return Vec3f.of(
            ((color >> 16) & 0xff) / 255.0f,
            ((color >> 8) & 0xff) / 255.0f,
            (color & 0xff) / 255.0f);
    }

    private void renderItem(T entity, EntityInfo info, ItemStack stack, TransformType type, ModelProperties.OriginPoint origin) {

        if(origin == null || stack.isEmpty())
            return;
        GlStateManager.pushMatrix();
        //transform to the parent's position, if applicable
        if(!origin.parent().isEmpty()) {

            ENIMModelRenderer parent = getStateManager().getModel(getCurrentEntityState()).getBox(origin.parent());
            parent.transformWithoutRendering(info);
        }
        //apply specified transformations
        float scale = 0.0625f * info.scale;
        Vec3f coords = origin.coords();
        GlStateManager.translate((coords.x - 8.0f) * scale, -coords.y * scale, (8.0f - coords.z) * scale);
        Vec3f rot = origin.rotation();
        Vec3f scl = origin.scale();
        GlStateManager.rotate(-rot.z, 0.0f, 0.0f, 1.0f);
        GlStateManager.rotate(-rot.y, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(+rot.x, 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(scl.x, scl.y, scl.z);
        //render
        boolean leftHand = type == TransformType.THIRD_PERSON_LEFT_HAND;
        Minecraft.getMinecraft().getItemRenderer().renderItemSide(entity, stack, type, leftHand);
        GlStateManager.popMatrix();
    }

    protected void rotateCorpse(T entity) {

        final float time = 15.0f; //ticks
        float rot = Entities.interpolate(0.0f, 90.0f, (float) entity.deathTime / time);
        GlStateManager.rotate(Math.min(rot, 90.0f), 0.0f, 0.0f, 1.0f);
    }

    @Override
    protected boolean canRenderName(T entity) {

        return entity.hasCustomName() &&
            Minecraft.isGuiEnabled() &&
            entity != renderManager.renderViewEntity &&
            !entity.isInvisibleToPlayer(Entities.thePlayer()) &&
            entity.getPassengers().isEmpty();
    }

    @Override
    public void renderName(T entity, double x, double y, double z) {

        if(canRenderName(entity)) {

            double distanceSq = entity.getDistanceSqToEntity(renderManager.renderViewEntity);
            if(distanceSq < NAMETAG_VISIBILITY_RANGE_SQ) {

                float namePos = getCurrentEntityState().model().properties().nameplate();
                float scale = 0.0625f * getCurrentEntityState().scale();
                EntityRenderer.drawNameplate(getFontRendererFromRenderManager(),
                    entity.getDisplayName().getFormattedText(),
                    (float) x,
                    (float) y + namePos * scale + (3.0f / 16.0f),
                    (float) z,
                    0,
                    renderManager.playerViewY,
                    renderManager.playerViewX,
                    renderManager.options.thirdPersonView == 2,
                    entity.isSneaking());
            }
        }
    }
}