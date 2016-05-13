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
	private final Map<AnimationType, Animation> animations = new EnumMap<>(AnimationType.class);

	@Override
	public void render(Entity entity, float time, float distance, float roll, float yaw, float pitch, float scale) {

		setRotationAngles(time, distance, roll, yaw, pitch, scale, entity);
		renderHelper(time, distance, roll, yaw, pitch, scale);
	}

	public void render(TileEntity tile, float time, float distance, float roll, float yaw, float pitch, float scale) {

		setRotationAngles(time, distance, roll, yaw, pitch, scale, tile);
		renderHelper(time, distance, roll, yaw, pitch, scale);
	}

	private void renderHelper(float time, float distance, float roll, float yaw, float pitch, float scale) {

		boxes.values().forEach(box -> box.render(scale));
	}

	private void setAnglesHelper(float time, float distance, float roll, float yaw, float pitch, float scale, Animation anim, int frame) {

		anim.frame(frame).forEach((define, angles) -> {

			ModelRenderer box = boxes.get(anim.toElementName(define));
			box.rotateAngleX = toRadians(angles[0]);
			box.rotateAngleY = toRadians(angles[1]);
			box.rotateAngleZ = toRadians(angles[2]);
		});
	}

	@Override
	public void setRotationAngles(float time, float distance, float roll, float yaw, float pitch, float scale, Entity entity) {

		Animation idle = animations.get(AnimationType.IDLE);
		if(idle != Animation.NO_OP) {

			int frame = (randomCounterFor(entity) + entity.ticksExisted) % idle.frameCount();
			if(frame < 0) frame += idle.frameCount();
			setAnglesHelper(time, distance, roll, yaw, pitch, scale, idle, frame);
		}
	}

	public void setRotationAngles(float time, float distance, float roll, float yaw, float pitch, float scale, TileEntity tile) {

		Animation idle = animations.get(AnimationType.IDLE);
		if(idle != Animation.NO_OP) {

			int frame = (randomCounterFor(tile) +
				(int) (tile.getWorld().getWorldTime() % Integer.MAX_VALUE)) % idle.frameCount();
			if(frame < 0) frame += idle.frameCount();
			setAnglesHelper(time, distance, roll, yaw, pitch, scale, idle, frame);
		}
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

			ModelRenderer box = new ENIMModelRenderer(this, m.name(), defrot, scale)
				.setTextureOffset(texcrds[0], texcrds[1]);
			box.setRotationPoint(rotpnt[0] - 8.0f, -rotpnt[1], 8.0f - rotpnt[2]);
			box.addBox(from[0] - rotpnt[0],
				rotpnt[1] - to[1],
				rotpnt[2] - to[2],
			(int)	(to[0] - from[0]),
			(int)	(to[1] - from[1]),
			(int)	(to[2] - from[2]));

			boxes.put(m.name(), box);
		}
		for(ModelElement m : elements) {

			ModelRenderer current = boxes.get(m.name());
			String parent = m.parent();
			if(boxes.containsKey(parent)) {

				boxes.get(parent).addChild(current);
				boxes.remove(m.name());
			}
		}
	}

	public final void setMissingno() {

		clearMaps();
		ModelRenderer missingno = new ModelRenderer(this, "#missingno");
		missingno.addBox(-8.0f, -16.0f, -8.0f, 16, 16, 16);
		boxes.put("#missingno", missingno);
	}

	private void clearMaps() {

		boxes.clear();
		animations.replaceAll((type, anim) -> Animation.NO_OP);
	}
}