package kvverti.enim.entity;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.function.ToIntBiFunction;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;

import kvverti.enim.abiescript.AbieScript;
import kvverti.enim.entity.animation.AnimType;
import kvverti.enim.entity.animation.EntityFrameTimers;
import kvverti.enim.entity.animation.MinecraftAnimTypes;
import kvverti.enim.model.EntityModel;
import kvverti.enim.model.Animation;
import kvverti.enim.model.ModelElement;
import kvverti.enim.Vec3f;
import kvverti.enim.Keys;

import static kvverti.enim.entity.Entities.*;

public class ENIMModel extends ModelBase {

//    public int textureWidth;
//    public int textureHeight;
    private final Map<String, ENIMModelRenderer> boxes = new HashMap<>();
    private final List<ENIMModelRenderer> opaques = new ArrayList<>();
    private final List<ENIMModelRenderer> lucents = new ArrayList<>();
    private final Map<AnimType, Animation> anims = new LinkedHashMap<>();

    public void render(GEntity entity, EntityInfo info) {

        setRotationAngles(entity, info);
        opaques.forEach(box -> box.render(info));
        lucents.forEach(box -> box.render(info));
    }

    public void setRotationAngles(GEntity entity, EntityInfo info) {

        resetAngles();
        for(Map.Entry<AnimType, Animation> entry : anims.entrySet()) {

            AnimType type = entry.getKey();
            Animation anim = entry.getValue();
            //cases where we do special things
            if(type.isLooped() && type.shouldAnimate(entity, info))
                animateLooping(type, entity, info, anim);
            else if(!type.isLooped())
                animateNoLooping(type, entity, info, anim);
        }
    }

    private void setAnglesHelper(Animation anim, int frame, EntityInfo info) {

        AbieScript.Frame f = anim.frame(frame, info.partialTicks);
        float speedScale = anim.scaling() == 0.0f ? 1.0f : anim.scaling() * (float) Math.sqrt(info.speedSq);
        for(String define : anim.defines()) {

            Vec3f[] forms = f.getTransforms(define);
            ENIMModelRenderer box = boxes.get(anim.toElementName(define));
            box.rotateAngleX = toRadians(forms[0].x) * speedScale;
            box.rotateAngleY = toRadians(forms[0].y) * speedScale;
            box.rotateAngleZ = toRadians(forms[0].z) * speedScale;
            box.shiftDistanceX = forms[1].x * speedScale;
            box.shiftDistanceY = forms[1].y * speedScale;
            box.shiftDistanceZ = forms[1].z * speedScale;
        }
    }

    private void animateLooping(AnimType type, GEntity entity, EntityInfo info, Animation anim) {

        int frame = (EntityFrameTimers.timeValue(type, entity, anim.tuning())
            & Integer.MAX_VALUE) % anim.frameCount();
        //kvverti.enim.Logger.info("Frame: %d", frame);
        setAnglesHelper(anim, frame, info);
    }

    private void animateNoLooping(AnimType type, GEntity entity, EntityInfo info, Animation anim) {

        if(type.shouldAnimate(entity, info))
            EntityFrameTimers.restart(type, entity);
        int frame = EntityFrameTimers.timeValue(type, entity, anim.tuning());
        if(frame >= 0 && frame < anim.frameCount())
            setAnglesHelper(anim, frame, info);
    }

    private void resetAngles() {

        boxes.values().forEach(box -> {

            box.rotateAngleX = 0.0f;
            box.rotateAngleY = 0.0f;
            box.rotateAngleZ = 0.0f;
            box.shiftDistanceX = 0.0f;
            box.shiftDistanceY = 0.0f;
            box.shiftDistanceZ = 0.0f;
        });
    }

    public final void reload(EntityModel model, int texSizeX, int texSizeY) {

        clearMaps();
        textureWidth = texSizeX;
        textureHeight = texSizeY;
        anims.putAll(model.animations());
        for(ModelElement m : model.elements()) {

            String name = m.name();
            ENIMModelRenderer box = new ENIMModelRenderer(this, m);
            boxes.put(name, box);
            if(m.isTranslucent()) lucents.add(box);
            else opaques.add(box);
        }
        for(ModelElement m : model.elements()) {

            ENIMModelRenderer current = boxes.get(m.name());
            String parent = m.parent();
            if(boxes.containsKey(parent)) {

                boxes.get(parent).addChild(current);
                lucents.remove(current);
                opaques.remove(current);
            }
        }
    }

    public final void setMissingno() {

        clearMaps();
        ENIMModelRenderer missingno = new ENIMModelRenderer(this);
        boxes.put("#missingno", missingno);
        opaques.add(missingno);
    }

    private void clearMaps() {

        boxes.clear();
        opaques.clear();
        lucents.clear();
        anims.clear();
    }

    public ENIMModelRenderer getBox(String name) {

        if(!boxes.containsKey(name))
            throw new IllegalArgumentException(name);
        return boxes.get(name);
    }

    /** @deprecated Replaced by {@link #render(GEntity, EntityInfo)} */
    @Override
    @Deprecated
    public void render(Entity entity, float f1, float f2, float f3, float f4, float f5, float f6) { }

    /** @deprecated Replaced by {@link #setRotationAngles(GEntity, EntityInfo)} */
    @Override
    @Deprecated
    public void setRotationAngles(float f1, float f2, float f3, float f4, float f5, float f6, Entity entity) { }

    /** @deprecated Replaced by {@link #setRotationAngles(GEntity, EntityInfo)} */
    @Override
    @Deprecated
    public void setLivingAnimations(net.minecraft.entity.EntityLivingBase entity, float f1, float f2, float f3) { }
}