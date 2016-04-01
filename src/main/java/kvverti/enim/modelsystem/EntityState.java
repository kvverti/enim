package kvverti.enim.modelsystem;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.entity.ENIMModel;

public final class EntityState {

	private final String name;
	private final ENIMModel model = new ENIMModel();
	private ResourceLocation modelFile;
	private float[] rotation;
	private float scale;
	private ResourceLocation image;
	private int xSize;
	private int ySize;

	public EntityState(String name) {

		this.name = name;
		model.setMissingno();
	}

	EntityState(String name, ResourceLocation modelLoc, float[] rots, float scale, ResourceLocation tex, int x, int y) {

		this.modelFile = modelLoc;
		this.name = name;
		parseModel(modelLoc);
		rotation = rots;
		this.scale = scale;
		image = tex;
		xSize = x;
		ySize = y;
	}

	public void reloadState(EntityState state) {

		this.rotation = state.rotation;
		this.modelFile = state.modelFile;
		this.scale = state.scale;
		this.image = state.image;
		this.xSize = state.xSize;
		this.ySize = state.ySize;
		parseModel(modelFile);
	}

	private final void parseModel(ResourceLocation loc) {

		Set<ModelElement> elements = new HashSet<>();
		try {
			EntityJsonParser parser = new EntityJsonParser(
				Minecraft.getMinecraft().getResourceManager().getResource(loc));
			parser.parseElements(elements);
			parser.getElementImports(elements);
			model.reloadModel(elements);

		} catch(ParserException|IOException e) {

			model.setMissingno();
		}
	}

	public int getXSize() {

		return xSize;
	}

	public int getYSize() {

		return ySize;
	}

	public ResourceLocation getTexture() {

		return image;
	}

	public ENIMModel getModel() {

		return model;
	}

	public float[] getRotation() {

		return rotation;
	}

	public float getScale() {

		return scale;
	}

	public String getName() {

		return name;
	}
}