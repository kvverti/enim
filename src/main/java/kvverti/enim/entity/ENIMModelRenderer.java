package kvverti.enim.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;

import kvverti.enim.Util;

import static kvverti.enim.entity.Entities.*;

public final class ENIMModelRenderer extends ModelRenderer {

	private static final Method compileDisplayList;
	private static final Field displayList;

	private final float[] defaultRotations;
	private final float defaultScale;
	private final boolean translucent;
	private boolean compiled = false;

	static {

		compileDisplayList = Util.findMethod(ModelRenderer.class,
			void.class,
			new String[] { "func_78788_d", "compileDisplayList" },
			float.class);

		displayList = Util.findField(ModelRenderer.class, int.class, "field_78811_r", "displayList");
	}

	public ENIMModelRenderer(ModelBase model, String boxName, float[] defRots, float scale, boolean translucent) {

		super(model, boxName);
		defaultRotations = defRots.clone();
		defaultScale = scale;
		this.translucent = translucent;
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
			GlStateManager.rotate(+toDegrees(rotateAngleZ), 0.0f, 0.0f, 1.0f);
			GlStateManager.rotate(+toDegrees(rotateAngleY), 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(-toDegrees(rotateAngleX), 1.0f, 0.0f, 0.0f);
			if(translucent) makeLucent();
			GlStateManager.callList(displayList());
			if(translucent) endLucent();
			if(childModels != null) childModels.forEach(box -> box.render(scale * defaultScale));
			GlStateManager.popMatrix();
		}
	}

	@Override
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