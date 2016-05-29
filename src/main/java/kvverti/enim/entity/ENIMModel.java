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

	private final Map<String, ModelRenderer> boxes = new HashMap<>();
	private final List<ModelRenderer> opaques = new ArrayList<>();
	private final List<ModelRenderer> lucents = new ArrayList<>();
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

		opaques.forEach(box -> box.render(scale));
		lucents.forEach(box -> box.render(scale));
	}

	private void setAnglesHelper(Animation anim, int frame) {

		anim.frame(frame).forEach((define, angles) -> {

			ModelRenderer box = boxes.get(anim.toElementName(define));
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
		if(speed > 0.0f) setAnglesHelper(anim, frame);
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

			float[] to = m.to();
			float[] from = m.from();
			int[] texcrds = m.texCoords();
			float[] rotpnt = m.rotationPoint();
			float[] defrot = m.defaultRotation();
			float scale = m.scale();
			boolean lucent = m.isTranslucent();

			ModelRenderer box = new ENIMModelRenderer(this, m.name(), defrot, scale, lucent)
				.setTextureOffset(texcrds[0], texcrds[1]);
			box.setRotationPoint(rotpnt[0] - 8.0f, -rotpnt[1], 8.0f - rotpnt[2]);
			box.addBox(from[0] - rotpnt[0],
				rotpnt[1] - to[1],
				rotpnt[2] - to[2],
			(int)	(to[0] - from[0]),
			(int)	(to[1] - from[1]),
			(int)	(to[2] - from[2]));

			boxes.put(m.name(), box);
			if(lucent) lucents.add(box);
			else opaques.add(box);
		}
		for(ModelElement m : elements) {

			ModelRenderer current = boxes.get(m.name());
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
		ModelRenderer missingno = new ModelRenderer(this, "#missingno");
		missingno.addBox(-8.0f, -16.0f, -8.0f, 16, 16, 16);
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