package kvverti.enim.entity;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import net.minecraft.client.model.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import kvverti.enim.modelsystem.Animation;
import kvverti.enim.modelsystem.AnimationType;
import kvverti.enim.modelsystem.ModelElement;

import static kvverti.enim.entity.Entities.*;

public class ENIMModel extends ModelBase {

//	public int textureWidth = 64;
//	public int textureHeight = 32;

	private final Map<String, ENIMModelRenderer> boxes = new HashMap<>();
	private final List<ENIMModelRenderer> opaques = new ArrayList<>();
	private final List<ENIMModelRenderer> lucents = new ArrayList<>();
	private final Map<AnimationType, Animation> animations = new EnumMap<>(AnimationType.class);

	@Override
	public void render(Entity entity, float speed, float dir, float timeExisted, float headYaw, float pitch, float scale) {

		setRotationAngles(speed, dir, timeExisted, headYaw, pitch, scale, entity);
		renderHelper(speed, dir, timeExisted, headYaw, pitch, scale);
	}

	public void render(TileEntity tile, float speed, float dir, float timeExisted, float headYaw, float pitch, float scale) {

		setRotationAngles(speed, dir, timeExisted, headYaw, pitch, scale, tile);
		renderHelper(speed, dir, timeExisted, headYaw, pitch, scale);
	}

	private void renderHelper(float speed, float dir, float timeExisted, float headYaw, float pitch, float scale) {

		opaques.forEach(box -> box.render(scale, headYaw, pitch));
		lucents.forEach(box -> box.render(scale, headYaw, pitch));
	}

	private void setAnglesHelper(Animation anim, int frame) {

		anim.frame(frame).forEach((define, angles) -> {

			ENIMModelRenderer box = boxes.get(anim.toElementName(define));
			box.rotateAngleX = toRadians(angles[0]);
			box.rotateAngleY = toRadians(angles[1]);
			box.rotateAngleZ = toRadians(angles[2]);
		});
	}

	@Override
	public void setRotationAngles(float speed, float dir, float timeExisted, float headYaw, float pitch, float scale, Entity entity) {

		//reset
		boxes.values().forEach(box -> {

			box.rotateAngleX = 0.0f;
			box.rotateAngleY = 0.0f;
			box.rotateAngleZ = 0.0f;
		});

		Animation anim = animations.get(AnimationType.IDLE);
		int frame = randomCounterFor(entity) % anim.frameCount();
		if(frame < 0) frame += anim.frameCount();
		setAnglesHelper(anim, frame);

		anim = animations.get(AnimationType.MOVE);
		frame = randomCounterFor(entity) % anim.frameCount();
		if(frame < 0) frame += anim.frameCount();
		if(speed > 0.05f) setAnglesHelper(anim, frame);

		anim = animations.get(AnimationType.AIR);
		frame = randomCounterFor(entity) % anim.frameCount();
		if(frame < 0) frame += anim.frameCount();
		if(entity.isAirBorne) setAnglesHelper(anim, frame);

	//	anim = animations.get(AnimationType.JUMP);
	//	frame = jumpTime(entity);
	//	kvverti.enim.Logger.info(frame);
	//	if(frame >= 0 && frame < anim.frameCount()) setAnglesHelper(anim, frame);
	}

	public void setRotationAngles(float speed, float dir, float timeExisted, float headYaw, float pitch, float scale, TileEntity tile) {

		Animation idle = animations.get(AnimationType.IDLE);
		int frame = randomCounterFor(tile) % idle.frameCount();
		if(frame < 0) frame += idle.frameCount();
		setAnglesHelper(idle, frame);
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
}