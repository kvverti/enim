package kvverti.enim.modelsystem;

import java.io.InputStream;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.EnumMap;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import kvverti.enim.Logger;
import kvverti.enim.entity.ENIMModel;
import kvverti.enim.entity.Entities;
import kvverti.enim.Util;

public final class EntityState {

	private final String name;
	private final ENIMModel model = new ENIMModel();
	private ResourceLocation modelFile;
	private float rotation;
	private float scale;
	private ResourceLocation image;
	private ResourceLocation imageLoc;
	private int xSize;
	private int ySize;

	public EntityState(String name) {

		this.name = name;
		model.setMissingno();
	}

	EntityState(String name, ResourceLocation modelLoc, float rot, float scale, ResourceLocation tex, int x, int y) {

		this.modelFile = modelLoc;
		this.name = name;
		parseModel(modelLoc);
		rotation = rot;
		this.scale = scale;
		image = bind(tex);
		imageLoc = tex;
		xSize = x;
		ySize = y;
	}

	public void reloadState(EntityState state) {

		this.rotation = state.rotation;
		this.modelFile = state.modelFile;
		this.scale = state.scale;
		this.image = state.image;
		this.imageLoc = state.imageLoc;
		this.xSize = state.xSize;
		this.ySize = state.ySize;
		parseModel(modelFile);
	}

	private final void parseModel(ResourceLocation loc) {

		Set<ModelElement> elements = new HashSet<>();
		Map<AnimationType, Animation> animations = new EnumMap<>(AnimationType.class);
		for(AnimationType type : AnimationType.values())
			animations.put(type, Animation.NO_OP);
		try {
			EntityJsonParser parser = new EntityJsonParser(Entities.resourceManager().getResource(loc));
			parser.getImports(elements, animations);
			parser.parseElements(elements);
			parser.applyOverrides(elements);
			parser.parseAnimations(animations);
			Set<String> elementNames = elements.stream()
				.map(ModelElement::name)
				.collect(Collectors.toSet());
			Util.validate(animations.values(), anim -> anim.validate(elementNames));
			model.reloadModel(elements, animations);

		} catch(ParserException|IOException e) {

			Logger.error(e);
			model.setMissingno();
			image = imageLoc = Util.MISSING_LOCATION;
		}
	}

	private ResourceLocation bind(ResourceLocation loc) {

		try(InputStream istream = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream()) {

			ResourceLocation tex = Entities.textureManager().getDynamicTextureLocation(
				"enim_entity_texture", new DynamicTexture(ImageIO.read(istream)));
			Entities.textureManager().bindTexture(tex);
			return tex;

		} catch(IOException e) {

			Logger.error("Could not bind texture for " + loc);
			return loc;
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

	public ResourceLocation textureFile() {

		return imageLoc;
	}

	public ENIMModel model() {

		return model;
	}

	public float rotation() {

		return rotation;
	}

	public float scale() {

		return scale;
	}

	public String name() {

		return name;
	}
}