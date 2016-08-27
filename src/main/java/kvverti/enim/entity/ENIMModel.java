package kvverti.enim.entity;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import kvverti.enim.modelsystem.Animation;
import kvverti.enim.modelsystem.AnimationType;
import kvverti.enim.modelsystem.ModelElement;

import static kvverti.enim.entity.Entities.*;

public class ENIMModel extends ModelBase {

//	public int textureWidth;
//	public int textureHeight;
	private final Map<String, ENIMModelRenderer> boxes = new HashMap<>();
	private final List<ENIMModelRenderer> opaques = new ArrayList<>();
	private final List<ENIMModelRenderer> lucents = new ArrayList<>();
	private final Map<AnimationType, Animation> animations = new EnumMap<>(AnimationType.class);

	public void render(Entity entity, EntityInfo info) {

		setRotationAngles(entity, info);
		renderHelper(info);
	}

	public void render(TileEntity tile, EntityInfo info) {

		setRotationAngles(tile, info);
		renderHelper(info);
	}

	private void renderHelper(EntityInfo info) {

		opaques.forEach(box -> box.render(info.scale));
		lucents.forEach(box -> box.render(info.scale));
	}

	public void setRotationAngles(Entity entity, EntityInfo info) {

		resetAngles(info.headYaw, info.entityPitch);
		animateLooping(entity, AnimationType.IDLE, true);
		animateLooping(entity, AnimationType.MOVE, info.speedSq > 0.0025f);
		animateLooping(entity, AnimationType.AIR, !entity.isInWater() && !entity.onGround);
		animateLooping(entity, AnimationType.SWIM, entity.isInWater() && !entity.onGround);
		animateNoLooping(AnimationType.JUMP, jumpTime(entity));
	}

	public void setRotationAngles(TileEntity tile, EntityInfo info) {

		resetAngles(info.headYaw, info.entityPitch);
		animateLooping(tile, AnimationType.IDLE, true);
	}

	private void setAnglesHelper(Animation anim, int frame) {

		anim.frame(frame).forEach((define, forms) -> {

			ENIMModelRenderer box = boxes.get(anim.toElementName(define));
			box.rotateAngleX = toRadians(forms[0]);
			box.rotateAngleY = toRadians(forms[1]);
			box.rotateAngleZ = toRadians(forms[2]);
			box.shiftDistanceX = forms[3] / 16.0f;
			box.shiftDistanceY = forms[4] / 16.0f;
			box.shiftDistanceZ = forms[5] / 16.0f;
		});
	}

	private void animateLooping(Entity entity, AnimationType type, boolean predicate) {

		if(predicate) {

			Animation anim = animations.get(type);
			int frame = randomCounterFor(entity) % anim.frameCount();
			if(frame < 0) frame += anim.frameCount();
			setAnglesHelper(anim, frame);
		}
	}

	private void animateLooping(TileEntity tile, AnimationType type, boolean predicate) {

		if(predicate) {

			Animation anim = animations.get(type);
			int frame = randomCounterFor(tile) % anim.frameCount();
			if(frame < 0) frame += anim.frameCount();
			setAnglesHelper(anim, frame);
		}
	}

	private void animateNoLooping(AnimationType type, int frame) {

		Animation anim = animations.get(type);
		if(frame >= 0 && frame < anim.frameCount())
			setAnglesHelper(anim, frame);
	}

	private void resetAngles(float headYaw, float pitch) {

		boxes.values().forEach(box -> {

			box.rotateAngleX = 0.0f;
			box.rotateAngleY = 0.0f;
			box.rotateAngleZ = 0.0f;
			box.shiftDistanceX = 0.0f;
			box.shiftDistanceY = 0.0f;
			box.shiftDistanceZ = 0.0f;
			box.headYaw = headYaw;
			box.pitch = pitch;
		});
	}

	public final void reloadModel(Set<ModelElement> elements, Map<AnimationType, Animation> animations) {

		clearMaps();
		this.animations.putAll(animations);
		for(ModelElement m : elements) {

			ENIMModelRenderer box = new ENIMModelRenderer(this, m);
			boxes.put(m.name(), box);
			if(m.isTranslucent()) lucents.add(box);
			else opaques.add(box);
		}
		for(ModelElement m : elements) {

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
		animations.replaceAll((type, anim) -> Animation.NO_OP);
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