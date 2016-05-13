package kvverti.enim.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

import kvverti.enim.Util;

import static kvverti.enim.entity.Entities.*;

public final class ENIMModelRenderer extends ModelRenderer {

	private static final Method compileDisplayList;
	private static final Field displayList;

	private final float[] defaultRotations;
	private final float defaultScale;
	private boolean compiled = false;

	static {

		compileDisplayList = ReflectionHelper.findMethod(
			ModelRenderer.class,
			null,
			new String[] { "func_78788_d", "compileDisplayList" },
			float.class);
		assert compileDisplayList.getReturnType() == void.class : "Type of compileDisplayList()";

		displayList = ReflectionHelper.findField(ModelRenderer.class, "field_78811_r", "displayList");
		assert displayList.getType() == int.class : "Type of displayList";
	}

	public ENIMModelRenderer(ModelBase model, String boxName, float[] defRots, float scale) {

		super(model, boxName);
		defaultRotations = defRots.clone();
		defaultScale = scale;
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
			if(boxName.equals("y")) rotateAngleY += 3.0f / 180.0f;
			if(boxName.equals("x")) rotateAngleX += 3.0f / 180.0f;
			if(boxName.equals("z")) rotateAngleZ += 3.0f / 180.0f;
			GlStateManager.rotate(toDegrees(rotateAngleZ), 0.0f, 0.0f, 1.0f);
			GlStateManager.rotate(toDegrees(rotateAngleY), 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(toDegrees(rotateAngleX), 1.0f, 0.0f, 0.0f);
			GlStateManager.callList(displayList());
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
			GlStateManager.rotate(toDegrees(rotateAngleZ), 0.0f, 0.0f, 1.0f);
			GlStateManager.rotate(toDegrees(rotateAngleY), 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(toDegrees(rotateAngleX), 1.0f, 0.0f, 0.0f);
			GlStateManager.callList(displayList());
			GlStateManager.popMatrix();
		}
	}

	private void compileDisplayList(float scale) {

		Util.invokeUnchecked(compileDisplayList, this, scale);
		compiled = true;
	}

	private int displayList() {

		return Util.getIntField(displayList, this);
	}
}