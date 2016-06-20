package kvverti.enim.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;

import kvverti.enim.Util;
import kvverti.enim.modelsystem.ModelElement;

import static kvverti.enim.entity.Entities.*;

public final class ENIMModelRenderer extends ModelRenderer {

	private static final Method compileDisplayList;
	private static final Field displayList;

	private final float[] defaultRotations;
	private final float defaultScale;
	private final boolean translucent;
	private final boolean head;
	private boolean compiled = false;
	public float headYaw;
	public float pitch;
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
		defaultRotations = new float[3];
		defaultScale = 1.0f;
		translucent = false;
		head = false;
		addBox(-8.0f, -16.0f, -8.0f, 16, 16, 16);
	}

	public ENIMModelRenderer(ModelBase model, ModelElement features) {

		super(model, features.name());
		defaultRotations = features.defaultRotation();
		defaultScale = features.scale();
		translucent = features.isTranslucent();
		head = features.isHead();
		float[] rotpnt = features.rotationPoint(), from = features.from(), to = features.to();
		int[] texcrds = features.texCoords();
		setTextureOffset(texcrds[0], texcrds[1]);
		setRotationPoint(rotpnt[0] - 8.0f, -rotpnt[1], 8.0f - rotpnt[2]);
		addBox(from[0] - rotpnt[0],
			rotpnt[1] - to[1],
			rotpnt[2] - to[2],
		(int)	(to[0] - from[0]),
		(int)	(to[1] - from[1]),
		(int)	(to[2] - from[2]));
	}

	@Override
	public void render(float scale) {

		if(!isHidden && showModel) {

			if(!compiled) compileDisplayList(scale * defaultScale);
			GlStateManager.pushMatrix();
			GlStateManager.translate(offsetX, offsetY, offsetZ);
			GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
			GlStateManager.rotate(+defaultRotations[2], 0.0f, 0.0f, 1.0f);
			GlStateManager.rotate(+defaultRotations[1], 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(-defaultRotations[0], 1.0f, 0.0f, 0.0f);
			if(head) {

				GlStateManager.rotate(headYaw, 0.0f, 1.0f, 0.0f);
				GlStateManager.rotate(pitch, 1.0f, 0.0f, 0.0f);
			}
			GlStateManager.translate(shiftDistanceX, -shiftDistanceY, -shiftDistanceZ);
			GlStateManager.rotate(+toDegrees(rotateAngleZ), 0.0f, 0.0f, 1.0f);
			GlStateManager.rotate(+toDegrees(rotateAngleY), 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(-toDegrees(rotateAngleX), 1.0f, 0.0f, 0.0f);
			if(translucent) makeLucent();
			GlStateManager.callList(displayList());
			if(translucent) endLucent();
			GlStateManager.scale(defaultScale, defaultScale, defaultScale);
			GlStateManager.translate(-rotationPointX * scale, -rotationPointY * scale, -rotationPointZ * scale);
			GlStateManager.translate(-offsetX, -offsetY, -offsetZ);
			if(childModels != null) childModels.forEach(box -> box.render(scale));
			GlStateManager.popMatrix();
		}
	}

	@Override
	@Deprecated
	public void renderWithRotation(float scale) {

		if(!isHidden && showModel) {

			if(!compiled) compileDisplayList(scale * defaultScale);
			GlStateManager.pushMatrix();
			GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
			GlStateManager.rotate(+defaultRotations[2], 0.0f, 0.0f, 1.0f);
			GlStateManager.rotate(+defaultRotations[1], 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(-defaultRotations[0], 1.0f, 0.0f, 0.0f);
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