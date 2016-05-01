package kvverti.enim.modelsystem;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.Logger;
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
		Map<AnimationType, Animation> animations = new HashMap<>();
		try {
			EntityJsonParser parser = new EntityJsonParser(
				Minecraft.getMinecraft().getResourceManager().getResource(loc));
			parser.parseElements(elements);
			parser.getElementImports(elements);
			parser.parseAnimations(animations);
			Set<String> elementNames = elements.stream()
				.map(ModelElement::name)
				.collect(Collectors.toSet());
			for(Animation anim : animations.values()) {

				anim.validate(elementNames);
			}
			model.reloadModel(elements, animations);

		} catch(ParserException|IOException e) {

			Logger.error(e);
			model.setMissingno();
		}
	}

	public int xSize() {

		return xSize;
	}

	public int ySize() {

		return ySize;
	}

	public ResourceLocation texture() {

		return image;
	}

	public ENIMModel model() {

		return model;
	}

	public float[] rotation() {

		return rotation;
	}

	public float scale() {

		return scale;
	}

	public String name() {

		return name;
	}
}