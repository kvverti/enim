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

//	public int textureWidth;
//	public int textureHeight;
	private final Map<String, ENIMModelRenderer> boxes = new HashMap<>();
	private final List<ENIMModelRenderer> opaques = new ArrayList<>();
	private final List<ENIMModelRenderer> lucents = new ArrayList<>();
	private final Map<AnimType, Animation> anims = new LinkedHashMap<>();

	public void render(Entity entity, EntityInfo info) {

		setRotationAngles(entity, info);
		renderHelper(info);
	}

	public void render(TileEntity tile, EntityInfo info) {

		setRotationAngles(tile, info);
		renderHelper(info);
	}

	private void renderHelper(EntityInfo info) {

		opaques.forEach(box -> box.render(info));
		lucents.forEach(box -> box.render(info));
	}

	public void setRotationAngles(Entity entity, EntityInfo info) {

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

	public void setRotationAngles(TileEntity tile, EntityInfo info) {

		resetAngles();
		animateLooping(tile, info, MinecraftAnimTypes.IDLE, true);
	}

	private void setAnglesHelper(Animation anim, int frame, float partialTicks) {

		AbieScript.Frame f = anim.frame(frame, partialTicks);
		for(String define : anim.defines()) {

			Vec3f[] forms = f.getTransforms(define);
			ENIMModelRenderer box = boxes.get(anim.toElementName(define));
			box.rotateAngleX = toRadians(forms[0].x);
			box.rotateAngleY = toRadians(forms[0].y);
			box.rotateAngleZ = toRadians(forms[0].z);
			box.shiftDistanceX = forms[1].x;
			box.shiftDistanceY = forms[1].y;
			box.shiftDistanceZ = forms[1].z;
		}
	}

	private void animateLooping(AnimType type, Entity entity, EntityInfo info, Animation anim) {

		int frame = (EntityFrameTimers.timeValue(type, entity, anim.shouldScaleWithMovement())
			& Integer.MAX_VALUE) % anim.frameCount();
		//kvverti.enim.Logger.info("Frame: %d", frame);
		setAnglesHelper(anim, frame, info.partialTicks);
	}

	private void animateLooping(TileEntity tile, EntityInfo info, AnimType type, boolean predicate) {

		if(anims.containsKey(type) && predicate) {

			Animation anim = anims.get(type);
			int frame = randomCounterFor(tile) % anim.frameCount();
			if(frame < 0) frame += anim.frameCount();
			setAnglesHelper(anim, frame, info.partialTicks);
		}
	}

	private void animateNoLooping(AnimType type, Entity entity, EntityInfo info, Animation anim) {

		if(type.shouldAnimate(entity, info))
			EntityFrameTimers.restart(type, entity);
		int frame = EntityFrameTimers.timeValue(type, entity, anim.shouldScaleWithMovement());
		if(frame >= 0 && frame < anim.frameCount())
			setAnglesHelper(anim, frame, info.partialTicks);
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

	/** @deprecated Replaced by {@link #render(Entity, EntityInfo)} */
	@Override
	@Deprecated
	public void render(Entity entity, float f1, float f2, float f3, float f4, float f5, float f6) { }

	/** @deprecated Replaced by {@link #setRotationAngles(Entity, EntityInfo)} */
	@Override
	@Deprecated
	public void setRotationAngles(float f1, float f2, float f3, float f4, float f5, float f6, Entity entity) { }

	/** @deprecated Replaced by {@link #setRotationAngles(Entity, EntityInfo)} */
	@Override
	@Deprecated
	public void setLivingAnimations(net.minecraft.entity.EntityLivingBase entity, float f1, float f2, float f3) { }
}