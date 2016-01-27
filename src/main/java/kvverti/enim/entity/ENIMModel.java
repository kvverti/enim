package kvverti.enim.entity;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import net.minecraft.client.model.*;
import net.minecraft.entity.Entity;

import kvverti.enim.modelsystem.ModelElement;

public class ENIMModel extends ModelBase {

//	public int textureWidth = 64;
//	public int textureHeight = 32;

	private final Map<String, ModelRenderer> boxes = new HashMap<>();

	@Override
	public void render(Entity entity, float time, float distance, float roll, float yaw, float pitch, float scale) {

		setRotationAngles(time, distance, roll, yaw, pitch, scale, entity);
		for(ModelRenderer m : boxes.values()) {

			m.render(scale);
		}
	}

	@Override
	public void setRotationAngles(float time, float distance, float roll, float yaw, float pitch, float scale, Entity entity) {

	}

	public final void reloadModel(Set<ModelElement> elements) {

		boxes.clear();
		for(ModelElement m : elements) {

			int[] to = m.getTo();
			int[] from = m.getFrom();
			int[] texcrds = m.getTexCoords();
			int[] rotpnt = m.getRotationPoint();

			ModelRenderer box = new ModelRenderer(this, m.getName()).setTextureOffset(texcrds[0], texcrds[1]);
			box.addBox(from[0] - 8, -to[1], from[2] - 8, to[0] - from[0], to[1] - from[1], to[2] - from[2]);
			box.setRotationPoint(rotpnt[0], rotpnt[1], rotpnt[2]);

			boxes.put(m.getName(), box);
		}
		for(ModelElement m : elements) {

			ModelRenderer current = boxes.get(m.getName());
			String parent = m.getParent();
			if(boxes.containsKey(parent)) {

				boxes.get(parent).addChild(current);
				boxes.remove(m.getName());
			}
		}
	}

	public final void setMissingno() {

		boxes.clear();
		ModelRenderer missingno = new ModelRenderer(this, "#missingno");
		missingno.addBox(-8.0f, -16.0f, -8.0f, 16, 16, 16);
		boxes.put("#missingno", missingno);
	}
}