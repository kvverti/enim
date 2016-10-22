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

import kvverti.enim.abiescript.AbieScript;
import kvverti.enim.model.EntityModel;
import kvverti.enim.model.Animation;
import kvverti.enim.model.ModelElement;
import kvverti.enim.Vec3f;

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

		resetAngles(info.headYaw, info.entityPitch);
		animateLooping(entity, Animation.Type.IDLE, true);
		animateLooping(entity, Animation.Type.MOVE, info.speedSq > 0.0025f);
		animateLooping(entity, Animation.Type.AIR, !entity.isInWater() && !entity.onGround);
		animateLooping(entity, Animation.Type.SWIM, entity.isInWater() && !entity.onGround);
		animateNoLooping(Animation.Type.JUMP, jumpTime(entity));
	}

	public void setRotationAngles(TileEntity tile, EntityInfo info) {

		resetAngles(info.headYaw, info.entityPitch);
		animateLooping(tile, Animation.Type.IDLE, true);
	}

	private void setAnglesHelper(Animation anim, int frame) {

		AbieScript.Frame f = anim.frame(frame);
		for(String define : anim.defines()) {

			Vec3f[] forms = f.getTransforms(define);
			ENIMModelRenderer box = boxes.get(anim.toElementName(define));
			box.rotateAngleX = toRadians(forms[0].x);
			box.rotateAngleY = toRadians(forms[0].y);
			box.rotateAngleZ = toRadians(forms[0].z);
			box.shiftDistanceX = forms[1].x / 16.0f;
			box.shiftDistanceY = forms[1].y / 16.0f;
			box.shiftDistanceZ = forms[1].z / 16.0f;
		}
	}

	private void animateLooping(Entity entity, Animation.Type type, boolean predicate) {

		if(anims.containsKey(type) && predicate) {

			Animation anim = anims.get(type);
			int frame = randomCounterFor(entity) % anim.frameCount();
			if(frame < 0) frame += anim.frameCount();
			setAnglesHelper(anim, frame);
		}
	}

	private void animateLooping(TileEntity tile, Animation.Type type, boolean predicate) {

		if(anims.containsKey(type) && predicate) {

			Animation anim = anims.get(type);
			int frame = randomCounterFor(tile) % anim.frameCount();
			if(frame < 0) frame += anim.frameCount();
			setAnglesHelper(anim, frame);
		}
	}

	private void animateNoLooping(Animation.Type type, int frame) {

		Animation anim = anims.get(type);
		if(anim != null && frame >= 0 && frame < anim.frameCount())
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

	public final void reload(EntityModel model, int texSizeX, int texSizeY) {

		clearMaps();
		textureWidth = texSizeX;
		textureHeight = texSizeY;
		anims.putAll(model.animations());
		for(ModelElement m : model.elements()) {

			ENIMModelRenderer box = new ENIMModelRenderer(this, m);
			boxes.put(m.name(), box);
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