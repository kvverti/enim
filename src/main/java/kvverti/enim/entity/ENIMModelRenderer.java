package kvverti.enim.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;

import kvverti.enim.Vec3f;
import kvverti.enim.Util;
import kvverti.enim.model.ModelElement;
import kvverti.enim.model.ModelProperties;

import static kvverti.enim.entity.Entities.*;

public class ENIMModelRenderer extends ModelRenderer {

	private static final Method compileDisplayList;
	private static final Field displayList;

	private final Vec3f defaultRotations;
	private final float defaultScale;
	private final boolean translucent;
	private final boolean head;
	private final int tintIndex;
	private boolean compiled = false;
	public float shiftDistanceX;
	public float shiftDistanceY;
	public float shiftDistanceZ;

	static {

		compileDisplayList = Util.findMethod(ModelRenderer.class,
			void.class,
			new String[] { "func_78788_d", "compileDisplayList" },
			float.class);

		displayList = Util.findField(ModelRenderer.class, int.class, "field_78811_r", "displayList");
	}

	/* Make missingno */
	ENIMModelRenderer(ModelBase model) {

		super(model, "#missingno");
		defaultRotations = Vec3f.ORIGIN;
		defaultScale = 1.0f;
		translucent = false;
		head = false;
		tintIndex = -1;
		addBox(-8.0f, -16.0f, -8.0f, 16, 16, 16);
	}

	public ENIMModelRenderer(ModelBase model, ModelElement features) {

		super(model, features.name());
		defaultRotations = features.rotation();
		defaultScale = features.scale();
		translucent = features.isTranslucent();
		head = features.isHead();
		tintIndex = features.tintIndex();
		Vec3f origin = features.origin(), from = features.from(), to = features.to();
		int[] uv = features.uv();
		setTextureOffset(uv[0], uv[1]);
		setRotationPoint(origin.x - 8.0f, -origin.y, 8.0f - origin.z);
		addBox(from.x - origin.x,
			origin.y - to.y,
			origin.z - to.z,
		(int)	(to.x - from.x),
		(int)	(to.y - from.y),
		(int)	(to.z - from.z));
	}

	public void render(EntityInfo info) {

		if(!isHidden && showModel) {

			float scale = info.scale;
			if(!compiled) compileDisplayList(scale);
			GlStateManager.pushMatrix();
			//transform element into position
			GlStateManager.translate(offsetX, offsetY, offsetZ);
			GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
			GlStateManager.rotate(-defaultRotations.z, 0.0f, 0.0f, 1.0f);
			GlStateManager.rotate(-defaultRotations.y, 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(+defaultRotations.x, 1.0f, 0.0f, 0.0f);
			//apply special transformations
			if(head) {

				GlStateManager.rotate(info.headYaw, 0.0f, 1.0f, 0.0f);
				GlStateManager.rotate(info.entityPitch, 1.0f, 0.0f, 0.0f);
			}
			//apply animations
			GlStateManager.translate(shiftDistanceX * scale, -shiftDistanceY * scale, -shiftDistanceZ * scale);
			GlStateManager.rotate(-toDegrees(rotateAngleZ), 0.0f, 0.0f, 1.0f);
			GlStateManager.rotate(-toDegrees(rotateAngleY), 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(+toDegrees(rotateAngleX), 1.0f, 0.0f, 0.0f);
			GlStateManager.scale(defaultScale, defaultScale, defaultScale);
			//render
			Vec3f color = info.color.apply(tintIndex);
			if(translucent || info.alpha < 1.0f) makeLucent();
			GlStateManager.color(color.x, color.y, color.z, info.alpha);
			GlStateManager.callList(displayList());
			if(translucent || info.alpha < 1.0f) endLucent();
			//do some transformations so children render properly
			GlStateManager.translate(-rotationPointX * scale, -rotationPointY * scale, -rotationPointZ * scale);
			GlStateManager.translate(-offsetX, -offsetY, -offsetZ);
			//render children
			if(childModels != null) childModels.forEach(box -> ((ENIMModelRenderer) box).render(info));
			GlStateManager.popMatrix();
		}
	}

	@Override
	@Deprecated
	public void render(float scale) {

		EntityInfo info = new EntityInfo();
		info.scale = scale;
		render(info);
	}

	@Override
	@Deprecated
	public void renderWithRotation(float scale) {

		if(!isHidden && showModel) {

			if(!compiled) compileDisplayList(scale * defaultScale);
			GlStateManager.pushMatrix();
			GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
			GlStateManager.rotate(+defaultRotations.z, 0.0f, 0.0f, 1.0f);
			GlStateManager.rotate(+defaultRotations.y, 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(-defaultRotations.x, 1.0f, 0.0f, 0.0f);
			GlStateManager.rotate(+toDegrees(rotateAngleZ), 0.0f, 0.0f, 1.0f);
			GlStateManager.rotate(+toDegrees(rotateAngleY), 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(-toDegrees(rotateAngleX), 1.0f, 0.0f, 0.0f);
			GlStateManager.callList(displayList());
			GlStateManager.popMatrix();
		}
	}

	private void makeLucent() {

		GlStateManager.enableNormalize();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 771);
	}

	private void endLucent() {

		GlStateManager.disableBlend();
		GlStateManager.disableNormalize();
	}

	private void compileDisplayList(float scale) {

		Util.invokeUnchecked(this, compileDisplayList, scale);
		compiled = true;
	}

	private int displayList() {

		return Util.getIntField(this, displayList);
	}
}