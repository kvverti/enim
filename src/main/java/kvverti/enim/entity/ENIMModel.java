package kvverti.enim.entity;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import net.minecraft.client.model.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

import kvverti.enim.modelsystem.ModelElement;

public class ENIMModel extends ModelBase {

//	public int textureWidth = 64;
//	public int textureHeight = 32;

	private final Map<String, ModelRenderer> boxes = new HashMap<>();
	private final List<ModelRenderer> parents = new ArrayList<>();
	private final Map<ModelRenderer, float[]> defaultRotations = new HashMap<>();
	private final Map<ModelRenderer, Float> scales = new HashMap<>();

	@Override
	public void render(Entity entity, float time, float distance, float roll, float yaw, float pitch, float scale) {

		setRotationAngles(time, distance, roll, yaw, pitch, scale, entity);
		float[] defRots;
		for(ModelRenderer m : boxes.values()) {

			if(m.boxName.equals("y")) m.rotateAngleY += 3.0f / 180.0f;
			if(m.boxName.equals("x")) m.rotateAngleX += 3.0f / 180.0f;
			if(m.boxName.equals("z")) m.rotateAngleZ += 3.0f / 180.0f;
			defRots = defaultRotations.get(m);
			GlStateManager.pushMatrix();
			GlStateManager.rotate(+defRots[2], 0.0f, 0.0f, 1.0f);
			GlStateManager.rotate(+defRots[1], 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(-defRots[0], 1.0f, 0.0f, 0.0f);
			if(parents.contains(m)) m.render(scale * scales.get(m));
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void setRotationAngles(float time, float distance, float roll, float yaw, float pitch, float scale, Entity entity) {

	}

	public final void reloadModel(Set<ModelElement> elements) {

		clearMaps();
		for(ModelElement m : elements) {

			float[] to = m.getTo();
			float[] from = m.getFrom();
			int[] texcrds = m.getTexCoords();
			float[] rotpnt = m.getRotationPoint();
			float[] defrot = m.getDefaultRotation();
			float scale = m.getScale();

			ModelRenderer box = new ModelRenderer(this, m.getName()).setTextureOffset(texcrds[0], texcrds[1]);
			box.setRotationPoint(rotpnt[0] - 8.0f, -rotpnt[1], 8.0f - rotpnt[2]);
			box.addBox(from[0] - rotpnt[0],
				rotpnt[1] - to[1],
				rotpnt[2] - to[2],
			(int)	(to[0] - from[0]),
			(int)	(to[1] - from[1]),
			(int)	(to[2] - from[2]));

			boxes.put(m.getName(), box);
			defaultRotations.put(box, defrot);
			scales.put(box, scale);
		}
		for(ModelElement m : elements) {

			ModelRenderer current = boxes.get(m.getName());
			String parent = m.getParent();
			if(boxes.containsKey(parent)) {

				boxes.get(parent).addChild(current);

			} else parents.add(current);
		}
	}

	public final void setMissingno() {

		clearMaps();
		ModelRenderer missingno = new ModelRenderer(this, "#missingno");
		missingno.addBox(-8.0f, -16.0f, -8.0f, 16, 16, 16);
		boxes.put("#missingno", missingno);
		defaultRotations.put(missingno, new float[] { 0.0f, 0.0f, 0.0f });
	}

	public final ModelRenderer getBox(String name) {

		return boxes.get(name);
	}

	private void clearMaps() {

		boxes.clear();
		parents.clear();
		defaultRotations.clear();
	}
}