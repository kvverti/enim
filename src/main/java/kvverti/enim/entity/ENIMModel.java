package kvverti.enim.entity;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;
import java.util.function.ToIntBiFunction;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;

import kvverti.enim.abiescript.AbieScript;
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
	private final Map<Animation.Type, Animation> anims = new EnumMap<>(Animation.Type.class);

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

		//kvverti.enim.Logger.info("Time: %d", randomCounterFor(entity));
		//kvverti.enim.Logger.info("Tick: %f", info.partialTicks);
		//kvverti.enim.Logger.info("Speed mult: %f", scalarFromSpeed(info.speedSq));
		resetAngles(info.headYaw, info.entityPitch);
		animateLooping(entity, info, Animation.Type.IDLE, true);
		animateLooping(entity, info, Animation.Type.MOVE, info.speedSq > 0.0025f);
		animateLooping(entity, info, Animation.Type.AIR, !entity.isInWater() && !entity.onGround);
		animateLooping(entity, info, Animation.Type.SWIM, entity.isInWater() && !entity.onGround);
		animateLooping(entity, info, Animation.Type.TRACK, hasAttackTarget(entity));
		animateNoLooping(entity, info, Animation.Type.JUMP, Entities::jumpTime);
	}

	public void setRotationAngles(TileEntity tile, EntityInfo info) {

		resetAngles(info.headYaw, info.entityPitch);
		animateLooping(tile, info, Animation.Type.IDLE, true);
	}

	private void setAnglesHelper(Animation anim, int frame) {

		AbieScript.Frame f = anim.frame(frame);
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

	private void animateLooping(Entity entity, EntityInfo info, Animation.Type type, boolean predicate) {

		if(anims.containsKey(type) && predicate) {

			Animation anim = anims.get(type);
			int frame = frameWithInterpolation(randomCounterFor(entity, anim.shouldScaleWithMovement()), info.partialTicks);
			frame = (frame & Integer.MAX_VALUE) % anim.frameCount();
			//kvverti.enim.Logger.info("Frame: %d", frame);
			setAnglesHelper(anim, frame);
		}
	}

	private void animateLooping(TileEntity tile, EntityInfo info, Animation.Type type, boolean predicate) {

		if(anims.containsKey(type) && predicate) {

			Animation anim = anims.get(type);
			int frame = frameWithInterpolation(randomCounterFor(tile), info.partialTicks) % anim.frameCount();
			if(frame < 0) frame += anim.frameCount();
			setAnglesHelper(anim, frame);
		}
	}

	private void animateNoLooping(Entity entity, EntityInfo info, Animation.Type type, ToIntBiFunction<Entity, Boolean> frameFunc) {

		if(anims.containsKey(type)) {

			Animation anim = anims.get(type);
			int frame = frameWithInterpolation(frameFunc.applyAsInt(entity, anim.shouldScaleWithMovement()), info.partialTicks);
			if(frame >= 0 && frame < anim.frameCount())
				setAnglesHelper(anim, frame);
		}
	}

	private int frameWithInterpolation(int original, float percent) {

		//important: must do integer overflow
		return original * Keys.INTERPOLATION_TICKS + (int) (percent * Keys.INTERPOLATION_TICKS);
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